package com.example.buscayaaguirre.repository

import androidx.room.withTransaction
import com.example.buscayaaguirre.data.local.AppDatabase
import com.example.buscayaaguirre.data.local.toEntity
import com.example.buscayaaguirre.data.local.toModel
import com.example.buscayaaguirre.model.Farmacia
import com.example.buscayaaguirre.model.SearchResponse
import com.example.buscayaaguirre.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio que maneja la fuente de datos (Room como única fuente de verdad y Retrofit para sincronización).
 */
class FarmaciasRepository(private val database: AppDatabase) {

    private val apiService = RetrofitClient.farmaciasService
    private val dao = database.farmaciaDao()
    private val negocioDao = database.negocioDao()

    // La UI observa directamente el Flow de la base de datos local (única fuente de verdad)
    val farmaciasFlow: Flow<List<Farmacia>> = dao.obtenerTodos().map { entities ->
        entities.map { it.toModel() }
    }

    /**
     * Obtiene un Flow reactivo de farmacias filtrado directamente por la base de datos Room usando la query LIKE del DAO.
     */
    fun buscarFarmaciasFlow(query: String = ""): Flow<List<Farmacia>> {
        val q = query.trim()
        val flow = if (q.isEmpty()) dao.obtenerTodos() else dao.buscarPorNombre(q)
        return flow.map { entities -> entities.map { it.toModel() } }
    }

    /**
     * Refresca los datos llamando a la API si la BD está vacía o si 'forzar' es true.
     * Utiliza withTransaction para garantizar atomicidad al borrar e insertar los nuevos datos.
     * Si ocurre un error de red (sin conexión), no se borra nada y se retorna failure,
     * permitiendo que la UI mantenga los datos almacenados en caché.
     */
    suspend fun refrescar(forzar: Boolean = false): Result<Unit> {
        return try {
            val count = dao.contar()
            if (forzar || count == 0) {
                val response = apiService.getFarmacias()
                if (response.farmacias != null || response.success == true) {
                    val farmaciasList = response.farmacias ?: emptyList()
                    val entities = farmaciasList.mapIndexed { index, farmacia ->
                        farmacia.toEntity(index = index + 1)
                    }
                    database.withTransaction {
                        dao.borrarTodos()
                        dao.insertarTodos(entities)
                    }
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message ?: "Error al obtener datos de la API"))
                }
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            android.util.Log.e("FarmaciasRepository", "Error al refrescar desde API", e)
            Result.failure(e)
        }
    }

    /**
     * Busca negocios llamando a la API y guarda los resultados en la BD local.
     * Si no hay conexión (ej. modo avión), consulta la BD local en Room para devolver
     * los negocios previamente buscados y guardados.
     */
    suspend fun searchNegocios(query: String): Result<SearchResponse> {
        return try {
            val response = apiService.searchNegocios(query)
            val results = response.results
            if (!results.isNullOrEmpty()) {
                val entities = results.map { it.toEntity(query) }
                negocioDao.insertarTodos(entities)
            }
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.w("FarmaciasRepository", "Fallo red en búsqueda, buscando en caché local", e)
            val locales = negocioDao.buscarLocalmente(query)
            val todos = if (locales.isEmpty()) negocioDao.obtenerTodos() else locales
            
            if (todos.isNotEmpty()) {
                val models = todos.map { it.toModel() }
                Result.success(
                    SearchResponse(
                        results = models,
                        total = models.size,
                        message = "Modo sin conexión: mostrando búsquedas guardadas"
                    )
                )
            } else {
                Result.failure(e)
            }
        }
    }
}
