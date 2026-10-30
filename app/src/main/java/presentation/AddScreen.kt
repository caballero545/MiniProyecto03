package com.example.miniproyecto03.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    viewModel: PublicacionViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fotoUriStr by remember { mutableStateOf("") }
    var bitmapPreview by remember { mutableStateOf<Bitmap?>(null) }

    val fechaActual = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    // Cámara (TakePicturePreview)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            bitmapPreview = bitmap
            fotoUriStr = saveBitmapToFile(context, bitmap)
        }
    }

    // Galería (GetContent)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val permanentPath = copyUriToFile(context, uri)
            if (permanentPath != null) {
                fotoUriStr = permanentPath
                bitmapPreview = null
            }
        }
    }

    // Permisos de Cámara en tiempo de ejecución
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Publicación") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Text(
                text = "Fecha: $fechaActual",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmapPreview != null) {
                    Image(
                        bitmap = bitmapPreview!!.asImageBitmap(),
                        contentDescription = "Foto cámara",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (fotoUriStr.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = fotoUriStr),
                        contentDescription = "Foto galería",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Ninguna imagen seleccionada")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val check = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (check == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cámara")
                }

                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Galería")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (titulo.isNotBlank() && descripcion.isNotBlank()) {
                        viewModel.agregarPublicacion(
                            titulo = titulo,
                            descripcion = descripcion,
                            fotoUri = fotoUriStr,
                            fecha = fechaActual
                        )
                        onBack()
                    } else {
                        Toast.makeText(context, "Llena todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Publicación")
            }
        }
    }
}

// Función auxiliar para persistir el Bitmap localmente en caché
fun saveBitmapToFile(context: Context, bitmap: Bitmap): String {
    val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
    val fos = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
    fos.flush()
    fos.close()
    return Uri.fromFile(file).toString()
}

// Función auxiliar para copiar cualquier URI (galería o archivos) a un archivo físico permanente en caché
fun copyUriToFile(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}