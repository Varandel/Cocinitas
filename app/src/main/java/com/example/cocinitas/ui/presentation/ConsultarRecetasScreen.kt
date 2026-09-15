package com.example.cocinitas.ui.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cocinitas.data.Receta
import com.example.cocinitas.data.TipoComida
import com.example.cocinitas.data.formatearMedida

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultarRecetasScreen(
    viewModel: RecetasViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recetas by viewModel.recetas.collectAsState()

    // Estados para la ordenación y filtrado
    var searchQuery by remember { mutableStateOf("") }
    var selectedComidas by remember { mutableStateOf(setOf<TipoComida>()) }
    var selectedAlimentos by remember { mutableStateOf(setOf<String>()) }

    val tiposAlimentosDisponibles = remember(recetas) {
        recetas.flatMap { it.tipoAlimentos }.toSet().sorted()
    }

    // Lógica de filtrado dinámico
    val recetasFiltradas = remember(recetas, searchQuery, selectedComidas, selectedAlimentos) {
        var lista = recetas
        if (searchQuery.isNotBlank()) {
            lista = lista.filter { it.nombre.contains(searchQuery, ignoreCase = true) }
        }
        if (selectedComidas.isNotEmpty()) {
            lista = lista.filter { receta ->
                selectedComidas.any { it in receta.tiposComida }
            }
        }
        if (selectedAlimentos.isNotEmpty()) {
            lista = lista.filter { receta ->
                selectedAlimentos.any { it in receta.tipoAlimentos }
            }
        }
        lista
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Recetas Guardadas (${recetasFiltradas.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarRecetas() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Controles de Búsqueda y Filtrado
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar por nombre...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = TipoComida.Comida in selectedComidas,
                            onClick = {
                                selectedComidas = if (TipoComida.Comida in selectedComidas) selectedComidas - TipoComida.Comida else selectedComidas + TipoComida.Comida
                            },
                            label = { Text("Comida") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = TipoComida.Cena in selectedComidas,
                            onClick = {
                                selectedComidas = if (TipoComida.Cena in selectedComidas) selectedComidas - TipoComida.Cena else selectedComidas + TipoComida.Cena
                            },
                            label = { Text("Cena") }
                        )
                    }

                    items(tiposAlimentosDisponibles) { alimento ->
                        FilterChip(
                            selected = alimento in selectedAlimentos,
                            onClick = {
                                selectedAlimentos = if (alimento in selectedAlimentos) selectedAlimentos - alimento else selectedAlimentos + alimento
                            },
                            label = { Text(alimento) }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

            if (recetasFiltradas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No se han encontrado recetas con estos filtros.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(recetasFiltradas, key = { it.nombre }) { receta ->
                        RecetaItemCard(receta = receta)
                    }
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
        // Reducimos el padding general de 16 a 8.dp para llegar más cerca del final de la tarjeta
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = receta.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Tipos de Comida (puede tener múltiples asignados)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        receta.tiposComida.forEach { tipo ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tipo.name, fontWeight = FontWeight.SemiBold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (tipo == TipoComida.Comida) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                                )
                            )
                        }
                    }
                }

                IconButton(onClick = { expandido = !expandido }) {
                    Icon(
                        imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandido) "Colapsar" else "Expandir"
                    )
                }
            }

            if (receta.tipoAlimentos.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
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

            AnimatedVisibility(visible = expandido) {
                Column(modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

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