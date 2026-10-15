package com.example.buscayaaguirre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.buscayaaguirre.model.Farmacia
import com.google.gson.Gson

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        val farmaciaJson = intent.getStringExtra("FARMACIA_DATA")
        val farmacia = Gson().fromJson(farmaciaJson, Farmacia::class.java)

        // Extraer lógica de limpieza de datos para consistencia
        val rawDireccion = farmacia?.direccion ?: ""
        val parts = if (rawDireccion.contains("–")) rawDireccion.split("–") 
                    else if (rawDireccion.contains("-")) rawDireccion.split("-")
                    else listOf(rawDireccion)
        
        val direccionLimpia = parts.firstOrNull()?.trim() ?: "Dirección no disponible"
        val telefonoExtraido = if (parts.size > 1) parts[1].trim() else null
        val telefonoFinal = farmacia?.telefono?.takeIf { it.isNotBlank() } ?: telefonoExtraido

        setContent {
            PharmacyDetailScreen(
                farmacia = farmacia,
                direccionLimpia = direccionLimpia,
                telefonoFinal = telefonoFinal,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyDetailScreen(
    farmacia: Farmacia?, 
    direccionLimpia: String, 
    telefonoFinal: String?, 
    onBack: () -> Unit
) {
    val headerBlue = Color(0xFF0D47A1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de Farmacia", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBlue)
            )
        }
    ) { innerPadding ->
        if (farmacia == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error al cargar datos")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
        ) {
            // Imagen de cabecera
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color.White)
            ) {
                if (!farmacia.imagen.isNullOrBlank()) {
                    val imageUrl = farmacia.imagen.replace("http://", "https://")
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = headerBlue)
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.serviya_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(100.dp)
                                )
                            }
                        },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = farmacia.nombre?.firstOrNull()?.toString() ?: "F",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerBlue.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de información principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (!farmacia.fechaRaw.isNullOrBlank()) {
                        Text(
                            text = "TURNO: ${farmacia.fechaRaw}",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = farmacia.nombre ?: "Farmacia",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerBlue
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFEEEEEE))

                    PharmacyInfoRow(icon = Icons.Default.LocationOn, label = "Dirección", value = direccionLimpia)
                    Spacer(modifier = Modifier.height(16.dp))
                    PharmacyInfoRow(icon = Icons.Default.Call, label = "Teléfono", value = telefonoFinal ?: "No disponible")
                    Spacer(modifier = Modifier.height(16.dp))
                    PharmacyInfoRow(icon = Icons.Default.Schedule, label = "Horario/Día", value = "${farmacia.diaSemana ?: ""} ${farmacia.horario ?: ""}".trim())
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PharmacyInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
        }
    }
}
