package com.example.buscayaaguirre.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.buscayaaguirre.model.BusinessResult

/**
 * Entidad de Room (@Entity) para almacenar negocios de las búsquedas localmente en la base de datos.
 */
@Entity(tableName = "negocios")
data class NegocioEntity(
    @PrimaryKey
    val id: String,
    val nombre: String?,
    val negocioNombre: String?,
    val descripcion: String?,
    val imagen: String?,
    val telefono: String?,
    val ubicacion: String?,
    val rating: Double?,
    val queryTag: String?
)

/**
 * Mapeo de DTO (BusinessResult) a Entidad de base de datos (NegocioEntity).
 */
fun BusinessResult.toEntity(queryTag: String = ""): NegocioEntity {
    val fallbackId = id ?: ((negocioNombre ?: nombre ?: "negocio") + "_" + (ubicacion?.hashCode() ?: System.currentTimeMillis()))
    return NegocioEntity(
        id = fallbackId,
        nombre = this.nombre,
        negocioNombre = this.negocioNombre,
        descripcion = this.descripcion,
        imagen = this.imagen,
        telefono = this.telefono,
        ubicacion = this.ubicacion,
        rating = this.rating,
        queryTag = queryTag
    )
}

/**
 * Mapeo de Entidad de base de datos (NegocioEntity) a DTO (BusinessResult).
 */
fun NegocioEntity.toModel(): BusinessResult {
    return BusinessResult(
        id = this.id,
        nombre = this.nombre,
        negocioNombre = this.negocioNombre,
        descripcion = this.descripcion,
        imagen = this.imagen,
        telefono = this.telefono,
        ubicacion = this.ubicacion,
        rating = this.rating
    )
}
