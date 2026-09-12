package com.example.cocinitas

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
// import com.example.cocinitas.data.SembradorRecetas
import com.example.cocinitas.ui.presentation.EnrutadorPrincipal
import com.example.cocinitas.ui.presentation.RecetasViewModel
import com.example.cocinitas.ui.theme.CocinitasTheme
import java.io.File

/**
 * Actividad principal de la aplicación Cocinitas.
 *
 * Se encarga de inicializar la interfaz de usuario con Jetpack Compose,
 * verificar los permisos de almacenamiento necesarios y configurar el
 * directorio principal para las recetas.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: RecetasViewModel by viewModels()

    /**
     * Punto de entrada de la actividad.
     *
     * Revisa al principio que tengamos los permisos de almacenamiento para consultar las recetas.
     *
     * Una vez los tengamos, obtenemos el directorio de Documentos, y cargamos toda la información de las carpetas.
     *
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        verificarPermisosAlmacenamiento()

        val carpetaPublica = obtenerDirectorioPublicoRecetas()
        // SembradorRecetas.sembrarSiEstaVacio(carpetaPublica)
        viewModel.configurarDirectorioYCargar(carpetaPublica)

        setContent {
            CocinitasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EnrutadorPrincipal(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /**
     * Obtiene y crea (si no existe) el directorio público para almacenar las recetas.
     *
     * La ruta utilizada es `Documentos/Cocinitas/recetas` dentro del
     * almacenamiento externo del dispositivo.
     *
     * @return [File] que representa el directorio de las recetas.
     */
    private fun obtenerDirectorioPublicoRecetas(): File {
        val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val dir = File(docs, "Cocinitas/recetas")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Verifica y solicita el permiso de acceso a todos los archivos
     * (MANAGE_APP_ALL_FILES_ACCESS_PERMISSION) para dispositivos con Android 11 (API 30) o superior.
     *
     * Si la aplicación no tiene el permiso concedido, redirige al usuario
     * a los ajustes del sistema para otorgarlo.
     */
    private fun verificarPermisosAlmacenamiento() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = "package:$packageName".toUri()
                }
                startActivity(intent)
            }
        }
    }
}