package com.example.cocinitas.ui.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cocinitas.data.Receta

enum class Pantalla { INICIO, CREAR, CONSULTAR, SELECCIONAR_MODIFICAR, MODIFICAR_FORMULARIO, ELIMINAR }

@Composable
fun EnrutadorPrincipal(viewModel: RecetasViewModel, modifier: Modifier = Modifier) {
    var pantallaActual by remember { mutableStateOf(Pantalla.INICIO) }
    var recetaAEditar by remember { mutableStateOf<Receta?>(null) }

    when (pantallaActual) {
        Pantalla.INICIO -> {
            PantallaInicio(
                onNavegarCrear = { pantallaActual = Pantalla.CREAR },
                onNavegarConsultar = { pantallaActual = Pantalla.CONSULTAR },
                onNavegarModificar = { pantallaActual = Pantalla.SELECCIONAR_MODIFICAR },
                onNavegarEliminar = { pantallaActual = Pantalla.ELIMINAR },
                modifier = modifier
            )
        }
        Pantalla.CREAR -> {
            CrearRecetaScreen(
                viewModel = viewModel,
                onVolver = { pantallaActual = Pantalla.INICIO },
                modifier = modifier
            )
        }
        Pantalla.CONSULTAR -> {
            ConsultarRecetasScreen(
                viewModel = viewModel,
                onVolver = { pantallaActual = Pantalla.INICIO },
                modifier = modifier
            )
        }
        Pantalla.SELECCIONAR_MODIFICAR -> {
            GestionarRecetasScreen(
                modo = ModoGestion.MODIFICAR,
                viewModel = viewModel,
                onSeleccionarModificar = { receta ->
                    recetaAEditar = receta
                    pantallaActual = Pantalla.MODIFICAR_FORMULARIO
                },
                onVolver = { pantallaActual = Pantalla.INICIO },
                modifier = modifier
            )
        }
        Pantalla.MODIFICAR_FORMULARIO -> {
            recetaAEditar?.let { receta ->
                ModificarRecetaScreen(
                    recetaOriginal = receta,
                    viewModel = viewModel,
                    onVolver = {
                        recetaAEditar = null
                        pantallaActual = Pantalla.INICIO
                    },
                    modifier = modifier
                )
            } ?: run {
                pantallaActual = Pantalla.INICIO
            }
        }
        Pantalla.ELIMINAR -> {
            GestionarRecetasScreen(
                modo = ModoGestion.ELIMINAR,
                viewModel = viewModel,
                onSeleccionarModificar = {},
                onVolver = { pantallaActual = Pantalla.INICIO },
                modifier = modifier
            )
        }
    }
}

@Composable
fun PantallaInicio(
    onNavegarCrear: () -> Unit,
    onNavegarConsultar: () -> Unit,
    onNavegarModificar: () -> Unit,
    onNavegarEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cocinitas",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Gestión local de recetas",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 36.dp)
        )

        Button(
            onClick = onNavegarCrear,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(vertical = 6.dp)
        ) {
            Text("Crear nueva receta", fontSize = 16.sp)
        }

        Button(
            onClick = onNavegarConsultar,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(vertical = 6.dp)
        ) {
            Text("Consultar recetas", fontSize = 16.sp)
        }

        Button(
            onClick = onNavegarModificar,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(vertical = 6.dp)
        ) {
            Text("Modificar receta ya existente", fontSize = 16.sp)
        }

        Button(
            onClick = onNavegarEliminar,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Eliminar receta", fontSize = 16.sp)
        }
    }
}