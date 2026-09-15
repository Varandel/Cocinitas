package com.example.cocinitas.ui.presentation

import android.widget.Toast
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cocinitas.data.Ingrediente
import com.example.cocinitas.data.MedidaIngrediente
import com.example.cocinitas.data.Receta
import com.example.cocinitas.data.TipoComida
import com.example.cocinitas.data.formatearMedida
import com.example.cocinitas.data.UnidadMedida

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificarRecetaScreen(
    recetaOriginal: Receta,
    viewModel: RecetasViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var nombreReceta by remember { mutableStateOf(recetaOriginal.nombre) }
    val tiposComidaSeleccionados = remember { mutableStateListOf<TipoComida>().apply { addAll(recetaOriginal.tiposComida) } }

    var textoTipoAlimento by remember { mutableStateOf("") }
    val tiposAlimentos = remember { mutableStateListOf<String>().apply { addAll(recetaOriginal.tipoAlimentos) } }

    var nombreIngrediente by remember { mutableStateOf("") }
    var valorMedida by remember { mutableStateOf("") }
    var unidadSeleccionada by remember { mutableStateOf(UnidadMedida.MASA) }
    val listaIngredientes = remember { mutableStateListOf<Ingrediente>().apply { addAll(recetaOriginal.ingredientes) } }

    var textoPaso by remember { mutableStateOf("") }
    val listaPasos = remember { mutableStateListOf<String>().apply { addAll(recetaOriginal.pasosReceta) } }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    )

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Modificando '${recetaOriginal.nombre}'", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nombreReceta,
                onValueChange = { nombreReceta = it },
                label = { Text("Nombre de la receta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )

            Text("Momento de comida (múltiple):", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = tiposComidaSeleccionados.contains(TipoComida.Comida),
                    onClick = {
                        if (tiposComidaSeleccionados.contains(TipoComida.Comida)) tiposComidaSeleccionados.remove(TipoComida.Comida)
                        else tiposComidaSeleccionados.add(TipoComida.Comida)
                    },
                    label = { Text("Comida") }
                )
                FilterChip(
                    selected = tiposComidaSeleccionados.contains(TipoComida.Cena),
                    onClick = {
                        if (tiposComidaSeleccionados.contains(TipoComida.Cena)) tiposComidaSeleccionados.remove(TipoComida.Cena)
                        else tiposComidaSeleccionados.add(TipoComida.Cena)
                    },
                    label = { Text("Cena") }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Text("Categorías / Tipo de Alimento:", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoTipoAlimento,
                    onValueChange = { textoTipoAlimento = it },
                    label = { Text("Añadir categoría...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    if (textoTipoAlimento.isNotBlank()) {
                        tiposAlimentos.add(textoTipoAlimento.trim())
                        textoTipoAlimento = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tiposAlimentos.forEach { tipo ->
                    InputChip(
                        selected = true,
                        onClick = { tiposAlimentos.remove(tipo) },
                        label = { Text(tipo) },
                        trailingIcon = { Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Text("Ingredientes:", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = nombreIngrediente,
                onValueChange = { nombreIngrediente = it },
                label = { Text("Nombre del ingrediente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (unidadSeleccionada == UnidadMedida.OTRO) {
                    OutlinedTextField(
                        value = valorMedida,
                        onValueChange = { valorMedida = it },
                        label = { Text("Descríbelo libremente") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = textFieldColors
                    )
                } else {
                    OutlinedTextField(
                        value = valorMedida,
                        onValueChange = { valorMedida = it },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = textFieldColors
                    )
                }

                Column(modifier = Modifier.weight(1.2f)) {
                    UnidadMedida.entries.forEach { unidad ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = unidadSeleccionada == unidad,
                                onClick = { unidadSeleccionada = unidad; valorMedida = "" }
                            )
                            Text(unidad.name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (nombreIngrediente.isNotBlank() && valorMedida.isNotBlank()) {
                        val medida = when (unidadSeleccionada) {
                            UnidadMedida.VOLUMEN -> MedidaIngrediente(volumenMl = valorMedida.toDoubleOrNull())
                            UnidadMedida.MASA -> MedidaIngrediente(masaGramos = valorMedida.toDoubleOrNull())
                            UnidadMedida.CANTIDAD -> MedidaIngrediente(cantidad = valorMedida.toIntOrNull())
                            UnidadMedida.OTRO -> MedidaIngrediente(textoLibre = valorMedida.trim())
                        }

                        if (unidadSeleccionada != UnidadMedida.OTRO && medida.volumenMl == null && medida.masaGramos == null && medida.cantidad == null) {
                            Toast.makeText(context, "El valor debe ser numérico/entero.", Toast.LENGTH_SHORT).show()
                        } else {
                            listaIngredientes.add(Ingrediente(nombreIngrediente.trim(), medida))
                            nombreIngrediente = ""
                            valorMedida = ""
                        }
                    } else {
                        Toast.makeText(context, "Indica un nombre y valor", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir Ingrediente")
            }

            listaIngredientes.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("• ${item.nombre} (${formatearMedida(item)})")
                    IconButton(onClick = { listaIngredientes.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Text("Pasos de la receta:", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoPaso,
                    onValueChange = { textoPaso = it },
                    label = { Text("Escribe un paso") },
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    if (textoPaso.isNotBlank()) {
                        listaPasos.add(textoPaso.trim())
                        textoPaso = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }

            listaPasos.forEachIndexed { index, paso ->
                var dragOffset by remember { mutableStateOf(0f) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Reordenar arrastrando",
                        modifier = Modifier
                            .pointerInput(paso) { // Localizar el elemento específico
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y
                                        val actualIdx = listaPasos.indexOf(paso)
                                        if (actualIdx != -1) {
                                            if (dragOffset > 50f && actualIdx < listaPasos.lastIndex) {
                                                val temp = listaPasos[actualIdx]
                                                listaPasos[actualIdx] = listaPasos[actualIdx + 1]
                                                listaPasos[actualIdx + 1] = temp
                                                dragOffset -= 50f
                                            } else if (dragOffset < -50f && actualIdx > 0) {
                                                val temp = listaPasos[actualIdx]
                                                listaPasos[actualIdx] = listaPasos[actualIdx - 1]
                                                listaPasos[actualIdx - 1] = temp
                                                dragOffset += 50f
                                            }
                                        }
                                    },
                                    onDragEnd = { dragOffset = 0f },
                                    onDragCancel = { dragOffset = 0f }
                                )
                            }
                            .padding(end = 12.dp)
                    )
                    Text("${index + 1}. $paso", modifier = Modifier.weight(1f))
                    IconButton(onClick = { listaPasos.remove(paso) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombreReceta.isBlank()) { Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show(); return@Button }
                    if (tiposComidaSeleccionados.isEmpty()) { Toast.makeText(context, "Selecciona al menos un momento de comida", Toast.LENGTH_SHORT).show(); return@Button }
                    if (listaIngredientes.isEmpty()) { Toast.makeText(context, "Añade al menos un ingrediente", Toast.LENGTH_SHORT).show(); return@Button }
                    if (listaPasos.isEmpty()) { Toast.makeText(context, "Añade al menos un paso", Toast.LENGTH_SHORT).show(); return@Button }

                    val recetaActualizada = recetaOriginal.copy(
                        nombre = nombreReceta.trim(),
                        tipoAlimentos = tiposAlimentos.toList(),
                        pasosReceta = listaPasos.toList(),
                        ingredientes = listaIngredientes.toList(),
                        tiposComida = tiposComidaSeleccionados.toList()
                    )

                    val exito = viewModel.modificarReceta(recetaOriginal.nombre, recetaActualizada)
                    if (exito) {
                        Toast.makeText(context, "Receta '${recetaOriginal.nombre}' actualizada", Toast.LENGTH_SHORT).show()
                        onVolver()
                    } else {
                        Toast.makeText(context, "Error al actualizar (¿El nuevo nombre ya existe?)", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Guardar Cambios", fontSize = 16.sp)
            }
        }
    }
}