package com.example.buscayaaguirre.network

import com.example.buscayaaguirre.model.FarmaciasResponse
import com.example.buscayaaguirre.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FarmaciasApiService {
    @GET("farmacias_turno_android")
    suspend fun getFarmacias(): FarmaciasResponse

    @GET("hybrid-search-android")
    suspend fun searchNegocios(
        @Query("q") query: String
    ): SearchResponse
}

