package com.example.buscayaaguirre.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.buscayaaguirre.model.Farmacia

/**
 * Entidad de Room (@Entity) para almacenar farmacias localmente en la base de datos.
 * Está separada del DTO de la API (Farmacia) para desacoplar la persistencia local
 * de los contratos de red.
 */
@Entity(tableName = "farmacias")
data class FarmaciaEntity(
    @PrimaryKey
    val id: Int,
    val nombre: String?,
    val direccion: String?,
    val telefono: String?,
    val latitud: Double?,
    val longitud: Double?,
    val horario: String?,
    val imagen: String?,
    val fechaRaw: String?,
    val fechaIso: String?,
    val diaSemana: String?,
    val fechaActualizacion: Long
)

/**
 * Función de extensión para mapear un DTO de la API (Farmacia) a una Entidad de Base de Datos (FarmaciaEntity).
 */
fun Farmacia.toEntity(index: Int = 0, fechaActualizacion: Long = System.currentTimeMillis()): FarmaciaEntity {
    val uniqueId = this.id ?: if (!this.fechaIso.isNullOrBlank()) Math.abs(this.fechaIso.hashCode()) else (index + 1)
    return FarmaciaEntity(
        id = uniqueId,
        nombre = this.nombre,
        direccion = this.direccion,
        telefono = this.telefono,
        latitud = this.latitud,
        longitud = this.longitud,
        horario = this.horario,
        imagen = this.imagen,
        fechaRaw = this.fechaRaw,
        fechaIso = this.fechaIso,
        diaSemana = this.diaSemana,
        fechaActualizacion = fechaActualizacion
    )
}

/**
 * Función de extensión para mapear una Entidad de Base de Datos (FarmaciaEntity) al modelo de UI (Farmacia).
 */
fun FarmaciaEntity.toModel(): Farmacia {
    return Farmacia(
        id = this.id,
        nombre = this.nombre,
        direccion = this.direccion,
        telefono = this.telefono,
        latitud = this.latitud,
        longitud = this.longitud,
        horario = this.horario,
        imagen = this.imagen,
        fechaRaw = this.fechaRaw,
        fechaIso = this.fechaIso,
        diaSemana = this.diaSemana
    )
}
