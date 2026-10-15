# Integración de Retrofit - API de Farmacias

## Estructura de archivos creados

Se han creado los siguientes archivos para la integración con la API de Farmacias:

### 1. **model/Farmacia.kt**
   - `Farmacia`: Clase de datos que representa una farmacia individual
   - `FarmaciasResponse`: Clase de datos que representa la respuesta de la API

### 2. **network/FarmaciasApiService.kt**
   - Interfaz de Retrofit que define el endpoint GET
   - Método `getFarmacias()` que hace la llamada a `farmacias_turno_android`

### 3. **network/RetrofitClient.kt**
   - Singleton que configura el cliente Retrofit
   - URL base: `https://buscaya.com.ar/api/`
   - Incluye OkHttp con logging e interceptores

### 4. **repository/FarmaciasRepository.kt**
   - Capa de repositorio que maneja las llamadas a la API
   - Retorna `Result<FarmaciasResponse>` para manejo de errores

### 5. **utils/FarmaciasViewModel.kt**
   - ViewModel para gestionar el estado de la UI
   - Usa Flow para observar cambios en tiempo real
   - Estados: Loading, Success, Error

## Cómo usar en tu MainActivity

### Opción 1: Llamada simple en un lado effect

```kotlin
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.buscayaaguirre.repository.FarmaciasRepository

@Composable
fun PharmacyListScreen(modifier: Modifier = Modifier) {
    val repository = remember { FarmaciasRepository() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            val resultado = repository.getFarmacias()
            resultado.onSuccess { response ->
                // Hacer algo con los datos
                Log.d("PharmacyList", "Farmacias: ${response.data}")
            }.onFailure { error ->
                Log.e("PharmacyList", "Error: ${error.message}")
            }
        }
    }
    
    // ... resto del composable
}
```

### Opción 2: Usar con ViewModel (Recomendado)

```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buscayaaguirre.utils.FarmaciasViewModel
import com.example.buscayaaguirre.utils.FarmaciasUiState

@Composable
fun PharmacyListScreen(
    modifier: Modifier = Modifier,
    viewModel: FarmaciasViewModel = viewModel()
) {
    val farmaciasState by viewModel.farmaciasState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.obtenerFarmacias()
    }
    
    when (farmaciasState) {
        FarmaciasUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is FarmaciasUiState.Success -> {
            val farmacias = (farmaciasState as FarmaciasUiState.Success).data.data ?: emptyList()
            // Mostrar las farmacias
            LazyColumn {
                items(farmacias) { farmacia ->
                    // Renderizar cada farmacia
                    Text(farmacia.nombre ?: "Sin nombre")
                }
            }
        }
        is FarmaciasUiState.Error -> {
            val mensaje = (farmaciasState as FarmaciasUiState.Error).message
            Text("Error: $mensaje")
        }
    }
}
```

## Dependencias agregadas

- **Retrofit**: 2.11.0
- **OkHttp**: 4.12.0
- **Gson Converter**: 2.11.0
- **Logging Interceptor**: 4.12.0

## Permisos añadidos

Se agregó el permiso `android.permission.INTERNET` en `AndroidManifest.xml`

## URL del endpoint

- **Base URL**: `https://buscaya.com.ar/api/`
- **Endpoint**: `farmacias_turno_android`
- **Método**: GET
- **URL completa**: `https://buscaya.com.ar/api/farmacias_turno_android`

## Cómo funciona

1. **RetrofitClient** crea una instancia única de Retrofit configurada con:
   - URL base
   - OkHttp client con logging
   - Gson converter para serializar/deserializar JSON

2. **FarmaciasApiService** define la interfaz con los endpoints disponibles

3. **FarmaciasRepository** encapsula la lógica de llamadas a la API

4. **FarmaciasViewModel** maneja el estado de la UI y ejecuta llamadas en threads separados

## Ejemplo de respuesta esperada

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "nombre": "Farmacia San Roque",
      "direccion": "Av. Illia 120",
      "telefono": "03573 412345",
      "latitud": -33.1234,
      "longitud": -64.5678,
      "horario": "08:00 - 22:00"
    }
  ],
  "message": "Success"
}
```

## Build completado

Para que los cambios surtan efecto, necesitas:

1. Hacer sync del proyecto con Gradle
2. Compilar la aplicación

Los cambios siguientes se han realizado automáticamente:
- ✅ `gradle/libs.versions.toml` - Agregadas versiones de Retrofit y OkHttp
- ✅ `app/build.gradle.kts` - Agregadas dependencias
- ✅ `AndroidManifest.xml` - Agregado permiso INTERNET
- ✅ Creados archivos de modelo, servicio, repositorio y ViewModel

