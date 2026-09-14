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
        _recetas.value = listaTemporal.sortedBy { it.idReceta }
    }

    fun guardarNuevaReceta(
        nombre: String,
        tipoAlimentos: List<String>,
        pasosReceta: List<String>,
        ingredientes: List<Ingrediente>,
        tipoComida: TipoComida
    ): Boolean {
        val dir = directorioRecetas ?: return false

        val siguienteId = (_recetas.value.maxOfOrNull { it.idReceta } ?: 0) + 1

        val nuevaReceta = Receta(
            idReceta = siguienteId,
            nombre = nombre.trim(),
            tipoAlimentos = tipoAlimentos,
            pasosReceta = pasosReceta,
            ingredientes = ingredientes,
            tipoComida = tipoComida
        )

        return try {
            // TODO: CAMBIAR NOMBRAMIENTO DE RECETA PARA QUE SE HAGA CON SU NOMBRE, NO CON SU ID
            val archivo = File(dir, "receta_${nuevaReceta.idReceta}.json")
            archivo.writeText(jsonConfig.encodeToString(nuevaReceta))
            _recetas.value = (_recetas.value + nuevaReceta).sortedBy { it.idReceta }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // NUEVO: Modifica una receta existente sobreescribiendo su JSON con el mismo idReceta
    fun modificarReceta(recetaModificada: Receta): Boolean {
        val dir = directorioRecetas ?: return false

        return try {
            val archivo = File(dir, "receta_${recetaModificada.idReceta}.json")
            archivo.writeText(jsonConfig.encodeToString(recetaModificada))

            // Reemplazamos la receta en la memoria RAM
            _recetas.value = _recetas.value.map {
                if (it.idReceta == recetaModificada.idReceta) recetaModificada else it
            }.sortedBy { it.idReceta }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // NUEVO: Elimina el archivo JSON físico y actualiza la lista en memoria RAM
    fun eliminarReceta(idReceta: Int): Boolean {
        val dir = directorioRecetas ?: return false

        return try {
            val archivo = File(dir, "receta_${idReceta}.json")
            val borradoExitoso = if (archivo.exists()) archivo.delete() else true

            if (borradoExitoso) {
                _recetas.value = _recetas.value.filterNot { it.idReceta == idReceta }
            }
            borradoExitoso
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}