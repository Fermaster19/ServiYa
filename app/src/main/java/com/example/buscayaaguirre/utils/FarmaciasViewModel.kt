package com.example.buscayaaguirre.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.buscayaaguirre.data.local.AppDatabase
import com.example.buscayaaguirre.model.Farmacia
import com.example.buscayaaguirre.model.SearchResponse
import com.example.buscayaaguirre.repository.FarmaciasRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * Estados sellados (Sealed Class) para representar de forma robusta el estado de la UI de farmacias.
 */
sealed class FarmaciasUiState {
    object Cargando : FarmaciasUiState()
    data class Exito(val farmacias: List<Farmacia>) : FarmaciasUiState()
    data class SinConexion(val farmacias: List<Farmacia>) : FarmaciasUiState()
    data class Error(val mensaje: String) : FarmaciasUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class FarmaciasViewModel(application: Application) : ViewModel() {

    private val database = AppDatabase.getInstance(application)
    private val repository = FarmaciasRepository(database)

    private val _localSearchQuery = MutableStateFlow("")
    val localSearchQuery: StateFlow<String> = _localSearchQuery.asStateFlow()

    private val _farmaciasState = MutableStateFlow<FarmaciasUiState>(FarmaciasUiState.Cargando)
    val farmaciasState: StateFlow<FarmaciasUiState> = _farmaciasState.asStateFlow()

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    init {
        // Observamos en tiempo real la consulta de Room filtrada directamente por el DAO mediante query LIKE
        viewModelScope.launch {
            _localSearchQuery.flatMapLatest { query ->
                repository.buscarFarmaciasFlow(query)
            }.collectLatest { lista ->
                val currentState = _farmaciasState.value
                if (currentState is FarmaciasUiState.SinConexion) {
                    _farmaciasState.value = FarmaciasUiState.SinConexion(lista)
                } else {
                    _farmaciasState.value = FarmaciasUiState.Exito(lista)
                }
            }
        }

        // Sincronizar al iniciar
        refrescar(forzar = false)
    }

    /**
     * Actualiza el texto de búsqueda. Al cambiar, activa la query LIKE en la base de datos Room.
     * Si la búsqueda se vacía, restaura el estado de búsqueda e inmediatamente vuelve a mostrar todas las farmacias.
     */
    fun onLocalSearchQueryChanged(nuevaQuery: String) {
        _localSearchQuery.value = nuevaQuery
        if (nuevaQuery.isBlank()) {
            _searchState.value = SearchUiState.Idle
        }
    }

    /**
     * Refresca los datos desde la API. Si no hay conexión y hay datos locales,
     * transiciona al estado SinConexion mostrando la caché.
     */
    fun refrescar(forzar: Boolean = false) {
        viewModelScope.launch {
            val listaActual = repository.farmaciasFlow.first()
            if (listaActual.isEmpty() && _farmaciasState.value !is FarmaciasUiState.Cargando) {
                _farmaciasState.value = FarmaciasUiState.Cargando
            }

            val resultado = repository.refrescar(forzar)
            resultado.onFailure { exception ->
                val actual = repository.farmaciasFlow.first()
                if (actual.isNotEmpty()) {
                    _farmaciasState.value = FarmaciasUiState.SinConexion(actual)
                } else {
                    val mensaje = when (exception) {
                        is java.net.UnknownHostException, is java.io.IOException -> 
                            "Sin conexión a Internet ni información almacenada localmente."
                        is retrofit2.HttpException -> when (exception.code()) {
                            403 -> "Error 403: Acceso restringido. Sin información almacenada."
                            404 -> "Error 404: Servicio no encontrado. Sin información almacenada."
                            else -> "Error en servidor (${exception.code()}). Sin información almacenada."
                        }
                        else -> "Sin conexión a Internet ni información almacenada."
                    }
                    _farmaciasState.value = FarmaciasUiState.Error(mensaje)
                }
            }
        }
    }

    fun buscarNegocios(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            try {
                val resultado = repository.searchNegocios(query)
                resultado.onSuccess { response ->
                    _searchState.value = SearchUiState.Success(response)
                }.onFailure { error ->
                    val msg = if (error is java.net.UnknownHostException || error is java.io.IOException) {
                        "Sin conexión a Internet ni información almacenada sobre '$query'."
                    } else {
                        error.message ?: "Sin conexión a Internet ni información almacenada."
                    }
                    _searchState.value = SearchUiState.Error(msg)
                }
            } catch (e: Exception) {
                _searchState.value = SearchUiState.Error(e.message ?: "Sin conexión a Internet ni información almacenada.")
            }
        }
    }
    
    fun clearSearch() {
        _searchState.value = SearchUiState.Idle
    }

    /**
     * Factory simple para instanciar el ViewModel sin requerir Hilt.
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FarmaciasViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FarmaciasViewModel(application) as T
            }
            throw IllegalArgumentException("Clase ViewModel desconocida")
        }
    }
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val data: SearchResponse) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
