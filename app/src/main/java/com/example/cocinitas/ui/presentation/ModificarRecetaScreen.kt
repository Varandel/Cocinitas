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
import androidx.compose.ui.unit.sp
import com.example.cocinitas.data.Ingrediente
import com.example.cocinitas.data.MedidaIngrediente
import com.example.cocinitas.data.Receta
import com.example.cocinitas.data.TipoComida

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificarRecetaScreen(
    recetaOriginal: Receta,
    viewModel: RecetasViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Inicializamos el formulario con los datos que ya tenía la receta
    var nombreReceta by remember { mutableStateOf(recetaOriginal.nombre) }
    var tipoComidaSeleccionado by remember { mutableStateOf(recetaOriginal.tipoComida) }

    var textoTipoAlimento by remember { mutableStateOf("") }
    val tiposAlimentos = remember { mutableStateListOf<String>().apply { addAll(recetaOriginal.tipoAlimentos) } }

    var nombreIngrediente by remember { mutableStateOf("") }
    var valorMedida by remember { mutableStateOf("") }
    var unidadSeleccionada by remember { mutableStateOf(UnidadMedida.MASA) }
    val listaIngredientes = remember { mutableStateListOf<Ingrediente>().apply { addAll(recetaOriginal.ingredientes) } }

    var textoPaso by remember { mutableStateOf("") }
    val listaPasos = remember { mutableStateListOf<String>().apply { addAll(recetaOriginal.pasosReceta) } }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Modificar Receta #${recetaOriginal.idReceta}") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            OutlinedTextField(
                value = nombreReceta,
                onValueChange = { nombreReceta = it },
                label = { Text("Nombre de la receta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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

            HorizontalDivider()

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

            HorizontalDivider()

            Text("Pasos de la receta:", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoPaso,
                    onValueChange = { textoPaso = it },
                    label = { Text("Escribe un paso") },
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
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
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

            Button(
                onClick = {
                    if (nombreReceta.isBlank()) {
                        Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (listaIngredientes.isEmpty()) {
                        Toast.makeText(context, "Añade al menos un ingrediente", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (listaPasos.isEmpty()) {
                        Toast.makeText(context, "Añade al menos un paso", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val recetaActualizada = recetaOriginal.copy(
                        nombre = nombreReceta.trim(),
                        tipoAlimentos = tiposAlimentos.toList(),
                        pasosReceta = listaPasos.toList(),
                        ingredientes = listaIngredientes.toList(),
                        tipoComida = tipoComidaSeleccionado
                    )

                    val exito = viewModel.modificarReceta(recetaActualizada)
                    if (exito) {
                        Toast.makeText(context, "Receta #${recetaOriginal.idReceta} actualizada", Toast.LENGTH_SHORT).show()
                        onVolver()
                    } else {
                        Toast.makeText(context, "Error al actualizar la receta", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Guardar Cambios", fontSize = 16.sp)
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