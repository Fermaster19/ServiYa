package com.example.buscayaaguirre.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operaciones de base de datos sobre la tabla farmacias.
 */
@Dao
interface FarmaciaDao {

    /**
     * Obtiene todas las farmacias almacenadas como un Flow para observación reactiva.
     */
    @Query("SELECT * FROM farmacias")
    fun obtenerTodos(): Flow<List<FarmaciaEntity>>

    /**
     * Inserta una lista de farmacias reemplazando las existentes en caso de conflicto.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(farmacias: List<FarmaciaEntity>)

    /**
     * Actualiza una farmacia existente.
     */
    @Update
    suspend fun actualizar(farmacia: FarmaciaEntity)

    /**
     * Elimina una farmacia de la base de datos.
     */
    @Delete
    suspend fun eliminar(farmacia: FarmaciaEntity)

    /**
     * Busca farmacias por nombre, dirección o teléfono directo desde la base de datos Room usando LIKE.
     * Parametrizado de forma segura para evitar inyección SQL.
     */
    @Query("SELECT * FROM farmacias WHERE nombre LIKE '%' || :query || '%' OR direccion LIKE '%' || :query || '%' OR telefono LIKE '%' || :query || '%'")
    fun buscarPorNombre(query: String): Flow<List<FarmaciaEntity>>

    /**
     * Retorna la cantidad total de registros en la tabla.
     */
    @Query("SELECT COUNT(*) FROM farmacias")
    suspend fun contar(): Int

    /**
     * Borra todos los registros de la tabla farmacias.
     */
    @Query("DELETE FROM farmacias")
    suspend fun borrarTodos()
}
