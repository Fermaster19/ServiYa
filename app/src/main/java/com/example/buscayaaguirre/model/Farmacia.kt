package com.example.buscayaaguirre.model

import com.google.gson.annotations.SerializedName

data class Farmacia(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("nombre")
    val nombre: String?,
    @SerializedName("direccion")
    val direccion: String?,
    @SerializedName("telefono")
    val telefono: String?,
    @SerializedName("latitud")
    val latitud: Double?,
    @SerializedName("longitud")
    val longitud: Double?,
    @SerializedName("horario")
    val horario: String?,
    @SerializedName("imagen")
    val imagen: String?,
    @SerializedName("fecha_raw")
    val fechaRaw: String?,
    @SerializedName("fecha_iso")
    val fechaIso: String?,
    @SerializedName("dia_semana")
    val diaSemana: String?
)

data class FarmaciasResponse(
    @SerializedName("success")
    val success: Boolean?,
    @SerializedName("farmacias")
    val farmacias: List<Farmacia>?,
    @SerializedName("message")
    val message: String?
)
