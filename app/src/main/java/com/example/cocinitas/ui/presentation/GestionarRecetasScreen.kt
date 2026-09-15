package com.example.cocinitas.ui.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cocinitas.data.Receta

enum class ModoGestion { MODIFICAR, ELIMINAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarRecetasScreen(
    modo: ModoGestion,
    viewModel: RecetasViewModel,
    onSeleccionarModificar: (Receta) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recetas by viewModel.recetas.collectAsState()
    var recetaParaEliminar by remember { mutableStateOf<Receta?>(null) }
    val titulo = if (modo == ModoGestion.MODIFICAR) "Modificar Receta" else "Eliminar Receta"

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(titulo, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        if (recetas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay recetas disponibles para gestionar.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(recetas, key = { it.nombre }) { receta ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = receta.nombre,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${receta.tiposComida.joinToString { it.name }} • ${receta.ingredientes.size} ingredientes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (modo == ModoGestion.MODIFICAR) {
                                Button(
                                    onClick = { onSeleccionarModificar(receta) }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Editar")
                                }
                            } else {
                                Button(
                                    onClick = { recetaParaEliminar = receta },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Borrar")
                                }
                            }
                        }
                    }
                }
            }
        }

        recetaParaEliminar?.let { receta ->
            val nombreLimpio = receta.nombre.replace(" ", "_").lowercase()
            AlertDialog(
                onDismissRequest = { recetaParaEliminar = null },
                title = { Text("¿Eliminar receta?") },
                text = { Text("Se eliminará definitivamente el archivo receta_$nombreLimpio.json de la receta \"${receta.nombre}\".") },
                confirmButton = {
                    Button(
                        onClick = {
                            val exito = viewModel.eliminarReceta(receta.nombre)
                            if (exito) {
                                Toast.makeText(context, "Receta eliminada de disco", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error al eliminar el archivo", Toast.LENGTH_LONG).show()
                            }
                            recetaParaEliminar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Sí, eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { recetaParaEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}