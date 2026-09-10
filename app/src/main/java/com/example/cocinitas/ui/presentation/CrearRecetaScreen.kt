package com.example.cocinitas.ui.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cocinitas.data.Ingrediente
import com.example.cocinitas.data.MedidaIngrediente
import com.example.cocinitas.data.TipoComida
import androidx.compose.ui.unit.sp

enum class UnidadMedida(val label: String) {
    VOLUMEN("Volumen (en mL)"),
    MASA("Masa (en gramos)"),
    CANTIDAD("Cantidad")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearRecetaScreen(
    viewModel: RecetasViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Campos principales
    var nombreReceta by remember { mutableStateOf("") }
    var tipoComidaSeleccionado by remember { mutableStateOf(TipoComida.Comida) }

    // Tipos de alimentos / Categorías
    var textoTipoAlimento by remember { mutableStateOf("") }
    val tiposAlimentos = remember { mutableStateListOf<String>() }

    // Ingredientes dinámicos
    var nombreIngrediente by remember { mutableStateOf("") }
    var valorMedida by remember { mutableStateOf("") }
    var unidadSeleccionada by remember { mutableStateOf(UnidadMedida.MASA) }
    val listaIngredientes = remember { mutableStateListOf<Ingrediente>() }

    // Pasos de receta ordenados
    var textoPaso by remember { mutableStateOf("") }
    val listaPasos = remember { mutableStateListOf<String>() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Nueva Receta") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
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
            // 1. Nombre
            OutlinedTextField(
                value = nombreReceta,
                onValueChange = { nombreReceta = it },
                label = { Text("Nombre de la receta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. Selector Tipo de Comida
            Text("Momento de comida:", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = tipoComidaSeleccionado == TipoComida.Comida,
                    onClick = { tipoComidaSeleccionado = TipoComida.Comida },
                    label = { Text("Comida") }
                )
                FilterChip(
                    selected = tipoComidaSeleccionado == TipoComida.Cena,
                    onClick = { tipoComidaSeleccionado = TipoComida.Cena },
                    label = { Text("Cena") }
                )
            }

            HorizontalDivider()

            // 3. Tipos de alimentos (Categorías)
            Text("Categorías / Tipo de Alimento:", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoTipoAlimento,
                    onValueChange = { textoTipoAlimento = it },
                    label = { Text("Ej: Carnes, Lácteos, Pasta...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textoTipoAlimento.isNotBlank()) {
                            tiposAlimentos.add(textoTipoAlimento.trim())
                            textoTipoAlimento = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Categoría")
                }
            }
            // Chips de categorías agregadas
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

            HorizontalDivider()

            // 4. Ingredientes
            Text("Ingredientes:", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = nombreIngrediente,
                onValueChange = { nombreIngrediente = it },
                label = { Text("Nombre del ingrediente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = valorMedida,
                    onValueChange = { valorMedida = it },
                    label = { Text("Cantidad numérica") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // Selector de unidad
                Column(modifier = Modifier.weight(1.2f)) {
                    UnidadMedida.entries.forEach { unidad ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = unidadSeleccionada == unidad,
                                onClick = { unidadSeleccionada = unidad }
                            )
                            Text(unidad.name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val valor = valorMedida.toDoubleOrNull()
                    if (nombreIngrediente.isNotBlank() && valor != null) {
                        val medida = when (unidadSeleccionada) {
                            UnidadMedida.VOLUMEN -> MedidaIngrediente(volumenMl = valor)
                            UnidadMedida.MASA -> MedidaIngrediente(masaGramos = valor)
                            UnidadMedida.CANTIDAD -> MedidaIngrediente(cantidad = valor)
                        }
                        listaIngredientes.add(Ingrediente(nombreIngrediente.trim(), medida))
                        nombreIngrediente = ""
                        valorMedida = ""
                    } else {
                        Toast.makeText(context, "Indica un nombre y valor numérico", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir Ingrediente a la lista")
            }

            // Lista de ingredientes añadidos
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

            HorizontalDivider()

            // 5. Pasos de la receta (ordenados)
            Text("Pasos de la receta (en orden):", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoPaso,
                    onValueChange = { textoPaso = it },
                    label = { Text("Escribe el siguiente paso") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textoPaso.isNotBlank()) {
                            listaPasos.add(textoPaso.trim())
                            textoPaso = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Paso")
                }
            }

            listaPasos.forEachIndexed { index, paso ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${index + 1}. $paso", modifier = Modifier.weight(1f))
                    IconButton(onClick = { listaPasos.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Botón de Guardado Final
            Button(
                onClick = {
                    if (nombreReceta.isBlank()) {
                        Toast.makeText(context, "El nombre de la receta es obligatorio", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (listaIngredientes.isEmpty()) {
                        Toast.makeText(context, "Añade al menos un ingrediente", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (listaPasos.isEmpty()) {
                        Toast.makeText(context, "Añade al menos un paso de preparación", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val exito = viewModel.guardarNuevaReceta(
                        nombre = nombreReceta,
                        tipoAlimentos = tiposAlimentos.toList(),
                        pasosReceta = listaPasos.toList(),
                        ingredientes = listaIngredientes.toList(),
                        tipoComida = tipoComidaSeleccionado
                    )

                    if (exito) {
                        Toast.makeText(context, "¡Receta guardada en disco!", Toast.LENGTH_SHORT).show()
                        onVolver()
                    } else {
                        Toast.makeText(context, "Error al guardar el archivo JSON", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Guardar Receta en JSON", fontSize = 16.sp)
            }
        }
    }
}

private fun formatearMedida(ingrediente: Ingrediente): String {
    val m = ingrediente.medidas
    return when {
        m.volumenMl != null -> "${m.volumenMl} mL"
        m.masaGramos != null -> "${m.masaGramos} g"
        m.cantidad != null -> "${m.cantidad} uds"
        else -> "Al gusto"
    }
}