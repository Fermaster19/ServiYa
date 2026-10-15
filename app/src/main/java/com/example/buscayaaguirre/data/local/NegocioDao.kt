package com.example.buscayaaguirre.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO para operaciones sobre la tabla de negocios cacheados.
 */
@Dao
interface NegocioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(negocios: List<NegocioEntity>)

    @Query("SELECT * FROM negocios WHERE nombre LIKE '%' || :query || '%' OR negocioNombre LIKE '%' || :query || '%' OR descripcion LIKE '%' || :query || '%' OR ubicacion LIKE '%' || :query || '%' OR queryTag LIKE '%' || :query || '%'")
    suspend fun buscarLocalmente(query: String): List<NegocioEntity>

    @Query("SELECT * FROM negocios")
    suspend fun obtenerTodos(): List<NegocioEntity>

    @Query("DELETE FROM negocios")
    suspend fun borrarTodos()
}
