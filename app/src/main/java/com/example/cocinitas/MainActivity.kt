package com.example.cocinitas

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
// import com.example.cocinitas.data.SembradorRecetas
import com.example.cocinitas.ui.presentation.EnrutadorPrincipal
import com.example.cocinitas.ui.presentation.RecetasViewModel
import com.example.cocinitas.ui.theme.CocinitasTheme
import java.io.File
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : ComponentActivity() {

    private val viewModel: RecetasViewModel by viewModels()

    // 1. Variables para controlar la descarga
    private var downloadId: Long = -1L
    private lateinit var onDownloadComplete: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        verificarPermisosAlmacenamiento()

        // 2. Configurar el listener que espera a que termine la descarga
        configurarReceptorDescargas()

        val carpetaPublica = obtenerDirectorioPublicoRecetas()
        // SembradorRecetas.sembrarSiEstaVacio(carpetaPublica)
        viewModel.configurarDirectorioYCargar(carpetaPublica)

        // ¡Aquí lanzamos la comprobación de actualizaciones de forma silenciosa!
        comprobarActualizaciones("Varandel", "Cocinitas")

        // EJEMPLO DE USO:
        // Llama a esta función cuando detectes que hay una nueva versión (ej. tras leer un JSON)
        // o asociarla a un botón en tu UI de Compose.
        // descargarYActualizarApk("https://tu-dominio.com/ruta/al/archivo.apk")

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

    // 3. Desregistrar el receiver al destruir la actividad para evitar memory leaks
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }

    // 4. Lógica de descarga e instalación
    private fun configurarReceptorDescargas() {
        onDownloadComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                // Verificamos que la descarga que terminó es la nuestra
                if (downloadId == id) {
                    val apkFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
                    if (apkFile.exists()) {
                        instalarApk(apkFile)
                    }
                }
            }
        }

        ContextCompat.registerReceiver(
            this,
            onDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun descargarYActualizarApk(url: String) {
        val apkFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
        if (apkFile.exists()) {
            apkFile.delete() // Borrar una actualización previa fallida o antigua
        }

        val request = DownloadManager.Request(url.toUri()).apply {
            setTitle("Actualizando Cocinitas")
            setDescription("Descargando la nueva versión...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            // Se guarda en los archivos internos de la app, en la carpeta Downloads
            setDestinationInExternalFilesDir(this@MainActivity, Environment.DIRECTORY_DOWNLOADS, "update.apk")
        }

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = manager.enqueue(request) // Guardamos el ID para el BroadcastReceiver
        Toast.makeText(this, "Descargando actualización...", Toast.LENGTH_SHORT).show()
    }

    private fun instalarApk(apkFile: File) {
        val contentUri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        // Si es Android 8.0+ verificamos si tenemos permiso para instalar orígenes desconocidos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                Toast.makeText(this, "Por favor, permite instalar actualizaciones para Cocinitas", Toast.LENGTH_LONG).show()

                val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = "package:$packageName".toUri()
                }
                startActivity(permissionIntent)
                return
            }
        }

        startActivity(intent)
    }

    // 5. Tus funciones originales se mantienen intactas
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
                    data = "package:$packageName".toUri()
                }
                startActivity(intent)
            }
        }
    }
    private fun comprobarActualizaciones(usuarioGithub: String, nombreRepo: String) {
        // Lanzamos la corrutina asociada al ciclo de vida de esta Activity
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.github.com/repos/$usuarioGithub/$nombreRepo/releases/latest")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.connectTimeout = 5000 // 5 segundos de tiempo de espera

                val codigo = connection.responseCode
                Log.d("Depuracion", "Código HTTP devuelto: $codigo")

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    // 1. Leemos el JSON de respuesta
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)

                    // 2. Extraemos el tag_name (ej: "v2") y sacamos solo el número (2)
                    val tagName = jsonObject.getString("tag_name")
                    val versionRemota = tagName.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0

                    // 3. Obtenemos el versionCode actual de nuestra app instalada
                    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(packageName, 0)
                    }

                    val versionActual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.versionCode
                    }

                    // 4. Comparamos las versiones
                    if (versionRemota > versionActual) {
                        // 5. Buscamos el archivo APK en el array "assets"
                        val assets = jsonObject.getJSONArray("assets")
                        if (assets.length() > 0) {
                            val apkUrl = assets.getJSONObject(0).getString("browser_download_url")

                            // 6. Volvemos al hilo principal para invocar a la interfaz de usuario
                            withContext(Dispatchers.Main) {
                                descargarYActualizarApk(apkUrl)
                            }
                        }
                    } else {
                        Log.d("Actualizaciones", "La app está en la última versión")
                    }
                }
            } catch (e: Exception) {
                // Si no hay internet o falla la API, lo ignoramos para no molestar al usuario
                Log.e("Actualizaciones", "Error al comprobar actualizaciones: ${e.message}")
            }
        }
    }
}