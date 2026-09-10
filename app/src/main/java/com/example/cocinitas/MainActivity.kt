package com.example.cocinitas

import android.content.Intent
import android.net.Uri
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
// import com.example.cocinitas.data.SembradorRecetas
import com.example.cocinitas.ui.presentation.EnrutadorPrincipal
import com.example.cocinitas.ui.presentation.RecetasViewModel
import com.example.cocinitas.ui.theme.CocinitasTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: RecetasViewModel by viewModels()

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

    private fun obtenerDirectorioPublicoRecetas(): File {
        val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val dir = File(docs, "Cocinitas/recetas")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun verificarPermisosAlmacenamiento() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }
}