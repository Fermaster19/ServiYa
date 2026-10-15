package com.example.buscayaaguirre.model

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("results")
    val results: List<BusinessResult>?,
    @SerializedName("total")
    val total: Int?,
    @SerializedName("message")
    val message: String?
)

data class BusinessResult(
    @SerializedName("id")
    val id: String?,
    @SerializedName("nombre")
    val nombre: String?,
    @SerializedName("negocio_nombre")
    val negocioNombre: String?,
    @SerializedName("descripcion")
    val descripcion: String?,
    @SerializedName("imagen")
    val imagen: String?,
    @SerializedName("telefono")
    val telefono: String?,
    @SerializedName("ubicacion")
    val ubicacion: String?,
    @SerializedName("rating")
    val rating: Double?
)
