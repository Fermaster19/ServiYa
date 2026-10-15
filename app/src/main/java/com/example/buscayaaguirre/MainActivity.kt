package com.example.buscayaaguirre

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.buscayaaguirre.model.BusinessResult
import com.example.buscayaaguirre.model.Farmacia
import com.example.buscayaaguirre.utils.FarmaciasUiState
import com.example.buscayaaguirre.utils.FarmaciasViewModel
import com.example.buscayaaguirre.utils.SearchUiState
import com.google.gson.Gson

class MainActivity : ComponentActivity() {

    private val viewModel: FarmaciasViewModel by viewModels {
        FarmaciasViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            val uiState by viewModel.farmaciasState.collectAsStateWithLifecycle()
            val searchState by viewModel.searchState.collectAsStateWithLifecycle()
            val searchQuery by viewModel.localSearchQuery.collectAsStateWithLifecycle()

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                PharmacyListScreen(
                    modifier = Modifier.padding(innerPadding),
                    uiState = uiState,
                    searchState = searchState,
                    searchQuery = searchQuery,
                    onQueryChange = { query -> viewModel.onLocalSearchQueryChanged(query) },
                    onRetry = { viewModel.refrescar(forzar = true) },
                    onRefresh = { viewModel.refrescar(forzar = true) },
                    onSearch = { query -> viewModel.buscarNegocios(query) }
                )
            }
        }
    }
}

@Composable
fun PharmacyListScreen(
    modifier: Modifier = Modifier,
    uiState: FarmaciasUiState,
    searchState: SearchUiState,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onSearch: (String) -> Unit
) {
    val headerBlue = Color(0xFF0D47A1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.serviya_logo),
                contentDescription = "ServiYa background logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.Crop
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .align(Alignment.BottomCenter)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "🔎", modifier = Modifier.padding(end = 8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { onQueryChange(it) },
                        placeholder = {
                            Text(
                                text = "Buscar lugar, dirección o teléfono",
                                color = Color.Black.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        textStyle = TextStyle(color = Color.Black, fontSize = 14.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Botones de Buscar y Actualizar ───────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onSearch(searchQuery) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(text = "Buscar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(24.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar",
                    tint = Color(0xFF0D47A1)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LazyColumn principal
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ── Aviso de Sin Conexión (si aplica) ────────────────────────────
            if (uiState is FarmaciasUiState.SinConexion) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF6C00),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sin conexión, mostrando datos guardados",
                                color = Color(0xFFE65100),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ── Resultados de Búsqueda ───────────────────────────────────────
            when (searchState) {
                is SearchUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                }
                is SearchUiState.Success -> {
                    val searchResults = searchState.data.results ?: emptyList()
                    if (searchResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Resultados encontrados",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = headerBlue
                            )
                        }
                        items(searchResults) { negocio ->
                            BusinessResultCard(negocio = negocio)
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
                is SearchUiState.Error -> {
                    item {
                        Text(
                            text = searchState.message,
                            color = Color.Red,
                            modifier = Modifier.padding(horizontal = 20.dp),
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {}
            }

            // ── Título Servicios Disponibles ─────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Servicios disponibles",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "(Farmacias en turno)", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Farmacias desde Room ─────────────────────────────────────────
            when (uiState) {
                is FarmaciasUiState.Cargando -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is FarmaciasUiState.Exito, is FarmaciasUiState.SinConexion -> {
                    val farmaciasDirectoDeRoom = when (uiState) {
                        is FarmaciasUiState.Exito -> uiState.farmacias
                        is FarmaciasUiState.SinConexion -> uiState.farmacias
                        else -> emptyList()
                    }

                    if (farmaciasDirectoDeRoom.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                val mensajeVacio = if (uiState is FarmaciasUiState.SinConexion || uiState is FarmaciasUiState.Error) {
                                    "Sin conexión a Internet ni información almacenada"
                                } else {
                                    "No se encontraron farmacias almacenadas"
                                }
                                Text(
                                    text = mensajeVacio,
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        items(farmaciasDirectoDeRoom) { farmacia ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                FarmaciaCard(farmacia = farmacia)
                            }
                        }
                    }
                }
                is FarmaciasUiState.Error -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Text(text = uiState.mensaje, color = Color.Red)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onRetry) { Text("Reintentar") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessResultCard(negocio: BusinessResult) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable {
                val intent = Intent(context, BusinessDetailActivity::class.java).apply {
                    putExtra("NEGOCIO_DATA", Gson().toJson(negocio))
                }
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!negocio.imagen.isNullOrBlank()) {
                    val imageUrl = negocio.imagen.replace("http://", "https://")
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(12.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF0D47A1)
                            )
                        },
                        error = {
                            Image(
                                painter = painterResource(R.drawable.serviya_logo),
                                contentDescription = null,
                                modifier = Modifier.padding(4.dp)
                            )
                        },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = negocio.negocioNombre ?: negocio.nombre ?: "Negocio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = negocio.ubicacion ?: "Sin ubicación",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!negocio.telefono.isNullOrBlank()) {
                    Text(
                        text = negocio.telefono,
                        fontSize = 13.sp,
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun FarmaciaCard(farmacia: Farmacia) {
    val context = LocalContext.current
    val rawDireccion = farmacia.direccion ?: ""
    val parts = if (rawDireccion.contains("–")) rawDireccion.split("–") 
                else if (rawDireccion.contains("-")) rawDireccion.split("-")
                else listOf(rawDireccion)
    
    val direccionLimpia = parts.firstOrNull()?.trim() ?: "Dirección no disponible"
    val telefonoExtraido = if (parts.size > 1) parts[1].trim() else null
    val telefonoFinal = farmacia.telefono?.takeIf { it.isNotBlank() } ?: telefonoExtraido

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra("FARMACIA_DATA", Gson().toJson(farmacia))
                }
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!farmacia.imagen.isNullOrBlank()) {
                    val imageUrl = farmacia.imagen.replace("http://", "https://")
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF1976D2)
                            )
                        },
                        error = {
                            Image(
                                painter = painterResource(R.drawable.serviya_logo),
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp)
                            )
                        },
                        contentDescription = "Logo de ${farmacia.nombre}",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    val initials = farmacia.nombre
                        ?.split(" ")
                        ?.mapNotNull { it.firstOrNull()?.toString() }
                        ?.take(2)
                        ?.joinToString("") ?: "F"
                    Text(
                        text = initials,
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (!farmacia.fechaRaw.isNullOrBlank()) {
                    Text(
                        text = farmacia.fechaRaw,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = farmacia.nombre ?: "Farmacia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = direccionLimpia,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = telefonoFinal ?: "Teléfono no disponible",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1976D2)
                    )
                }

                if (!farmacia.horario.isNullOrEmpty() || !farmacia.diaSemana.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val infoExtra = listOfNotNull(farmacia.diaSemana, farmacia.horario).joinToString(" - ")
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = infoExtra,
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PharmacyListPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text("Pharmacy List Preview")
    }
}
