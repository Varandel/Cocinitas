package com.example.cocinitas.ui.presentation

import androidx.lifecycle.ViewModel
import com.example.cocinitas.data.Ingrediente
import com.example.cocinitas.data.Receta
import com.example.cocinitas.data.TipoComida
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.io.File

class RecetasViewModel : ViewModel() {

    private val _recetas = MutableStateFlow<List<Receta>>(emptyList())
    val recetas: StateFlow<List<Receta>> = _recetas.asStateFlow()

    var directorioRecetas: File? = null
        private set

    private val jsonConfig = Json {
        encodeDefaults = false
        explicitNulls = false
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun configurarDirectorioYCargar(directorio: File) {
        this.directorioRecetas = directorio
        cargarRecetas()
    }

    fun cargarRecetas() {
        val dir = directorioRecetas ?: return
        if (!dir.exists()) return

        val listaTemporal = mutableListOf<Receta>()
        val archivosJson = dir.listFiles { _, name -> name.endsWith(".json") }

        archivosJson?.forEach { archivo ->
            try {
                val receta = jsonConfig.decodeFromString<Receta>(archivo.readText())
                listaTemporal.add(receta)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Ordenamos alfabéticamente por defecto
        _recetas.value = listaTemporal.sortedBy { it.nombre.lowercase() }
    }

    private fun limpiarNombreArchivo(nombre: String): String {
        return nombre.replace(" ", "_").lowercase()
    }

    fun guardarNuevaReceta(
        nombre: String,
        tipoAlimentos: List<String>,
        pasosReceta: List<String>,
        ingredientes: List<Ingrediente>,
        tiposComida: List<TipoComida>
    ): Boolean {
        val dir = directorioRecetas ?: return false

        // Validación: El nombre debe ser único
        if (_recetas.value.any { it.nombre.equals(nombre.trim(), ignoreCase = true) }) {
            return false
        }

        val nuevaReceta = Receta(
            nombre = nombre.trim(),
            tipoAlimentos = tipoAlimentos,
            pasosReceta = pasosReceta,
            ingredientes = ingredientes,
            tiposComida = tiposComida
        )

        return try {
            val archivo = File(dir, "receta_${limpiarNombreArchivo(nuevaReceta.nombre)}.json")
            archivo.writeText(jsonConfig.encodeToString(nuevaReceta))
            _recetas.value = (_recetas.value + nuevaReceta).sortedBy { it.nombre.lowercase() }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun modificarReceta(nombreOriginal: String, recetaModificada: Receta): Boolean {
        val dir = directorioRecetas ?: return false

        // Comprobamos si ha cambiado el nombre y si el nuevo ya existe
        if (!nombreOriginal.equals(recetaModificada.nombre, ignoreCase = true) &&
            _recetas.value.any { it.nombre.equals(recetaModificada.nombre, ignoreCase = true) }) {
            return false // El nuevo nombre de la receta ya está cogido por otra diferente
        }

        return try {
            // Borramos el JSON antiguo
            val archivoAntiguo = File(dir, "receta_${limpiarNombreArchivo(nombreOriginal)}.json")
            if (archivoAntiguo.exists()) archivoAntiguo.delete()

            // Guardamos el nuevo JSON
            val archivoNuevo = File(dir, "receta_${limpiarNombreArchivo(recetaModificada.nombre)}.json")
            archivoNuevo.writeText(jsonConfig.encodeToString(recetaModificada))

            // Reemplazamos la receta en la memoria RAM
            _recetas.value = _recetas.value
                .filterNot { it.nombre == nombreOriginal }
                .plus(recetaModificada)
                .sortedBy { it.nombre.lowercase() }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun eliminarReceta(nombreReceta: String): Boolean {
        val dir = directorioRecetas ?: return false

        return try {
            val archivo = File(dir, "receta_${limpiarNombreArchivo(nombreReceta)}.json")
            val borradoExitoso = if (archivo.exists()) archivo.delete() else true

            if (borradoExitoso) {
                _recetas.value = _recetas.value.filterNot { it.nombre == nombreReceta }
            }
            borradoExitoso
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}