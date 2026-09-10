package com.example.cocinitas.ui.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cocinitas.data.Ingrediente
import com.example.cocinitas.data.Receta
import com.example.cocinitas.data.TipoComida

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultarRecetasScreen(
    viewModel: RecetasViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recetas by viewModel.recetas.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Recetas Guardadas (${recetas.size})") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarRecetas() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }
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
                    text = "No hay recetas disponibles.\n¡Crea una nueva desde el menú principal!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(recetas, key = { it.idReceta }) { receta ->
                    RecetaItemCard(receta = receta)
                }
            }
        }
    }
}

@Composable
fun RecetaItemCard(receta: Receta) {
    var expandido by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = !expandido },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#${receta.idReceta} - ${receta.nombre}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Chip para Tipo de Comida (Comida o Cena)
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = receta.tipoComida.name,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (receta.tipoComida == TipoComida.Comida)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                }

                IconButton(onClick = { expandido = !expandido }) {
                    Icon(
                        imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandido) "Colapsar" else "Expandir"
                    )
                }
            }

            // Etiquetas de Tipo de Alimentos
            if (receta.tipoAlimentos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    receta.tipoAlimentos.forEach { tipo ->
                        AssistChip(
                            onClick = {},
                            label = { Text(tipo, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Contenido expandible (Ingredientes y Pasos)
            AnimatedVisibility(visible = expandido) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Ingredientes:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    receta.ingredientes.forEach { ingrediente ->
                        Text(
                            text = "• ${ingrediente.nombre}: ${formatearMedida(ingrediente)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Pasos de preparación:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    receta.pasosReceta.forEachIndexed { index, paso ->
                        Text(
                            text = "${index + 1}. $paso",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Auxiliar para mostrar únicamente la medida que contenga el ingrediente
private fun formatearMedida(ingrediente: Ingrediente): String {
    val m = ingrediente.medidas
    return when {
        m.volumenMl != null -> "${m.volumenMl} mL"
        m.masaGramos != null -> "${m.masaGramos} g"
        m.cantidad != null -> "${m.cantidad} uds"
        else -> "Al gusto"
    }
}