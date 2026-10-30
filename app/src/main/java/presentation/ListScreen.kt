package com.example.miniproyecto03.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.miniproyecto03.data.Publicacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    viewModel: PublicacionViewModel,
    onNavigateToAdd: () -> Unit
) {
    val publicaciones by viewModel.publicaciones.collectAsState()

    val pubsConFoto = remember(publicaciones) {
        publicaciones.filter { it.fotoUri.isNotEmpty() }
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Red Social Local") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Publicación")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (publicaciones.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay publicaciones registradas.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(publicaciones) { pub ->
                        PublicacionItem(
                            publicacion = pub,
                            onDelete = { viewModel.eliminarPublicacion(pub) },
                            onImageClick = {
                                if (pub.fotoUri.isNotEmpty()) {
                                    val index = pubsConFoto.indexOf(pub)
                                    if (index != -1) {
                                        selectedIndex = index
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (selectedIndex != null && pubsConFoto.isNotEmpty()) {
            Dialog(
                onDismissRequest = { selectedIndex = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                val pagerState = rememberPagerState(initialPage = selectedIndex!!) {
                    pubsConFoto.size
                }

                val scales = remember { mutableStateMapOf<Int, Float>() }
                val offsetsX = remember { mutableStateMapOf<Int, Float>() }
                val offsetsY = remember { mutableStateMapOf<Int, Float>() }

                val currentScale = scales[pagerState.currentPage] ?: 1f
                val currentPub = pubsConFoto[pagerState.currentPage]

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                ) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = currentScale == 1f,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val scale = scales[page] ?: 1f
                        val offsetX = offsetsX[page] ?: 0f
                        val offsetY = offsetsY[page] ?: 0f
                        val pub = pubsConFoto[page]

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { })
                                }
                                .pointerInput(page) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        var pointerPressed = true
                                        do {
                                            val event = awaitPointerEvent()
                                            val activePointers = event.changes.count { it.pressed }
                                            val activeScale = scales[page] ?: 1f

                                            if (activePointers >= 2 || activeScale > 1f) {
                                                val zoom = event.calculateZoom()
                                                val pan = event.calculatePan()

                                                event.changes.forEach { it.consume() }

                                                val newScale = (activeScale * zoom).coerceIn(1f, 4f)
                                                scales[page] = newScale

                                                if (newScale <= 1f) {
                                                    offsetsX[page] = 0f
                                                    offsetsY[page] = 0f
                                                } else {
                                                    offsetsX[page] = (offsetsX[page] ?: 0f) + pan.x
                                                    offsetsY[page] = (offsetsY[page] ?: 0f) + pan.y
                                                }
                                            }
                                            pointerPressed = event.changes.any { it.pressed }
                                        } while (pointerPressed)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = pub.fotoUri,
                                contentDescription = "Imagen ampliada",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY
                                    )
                            )
                        }
                    }

                    // Título en la parte superior con botón de cerrar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentPub.titulo,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { selectedIndex = null }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Descripción en la parte inferior con opción "Leer más / Leer menos"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        ExpandableDescription(
                            description = currentPub.descripcion,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PublicacionItem(publicacion: Publicacion, onDelete: () -> Unit, onImageClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = publicacion.titulo,
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = publicacion.fecha,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (publicacion.fotoUri.isNotEmpty()) {
                AsyncImage(
                    model = publicacion.fotoUri,
                    contentDescription = "Imagen de la publicación",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onImageClick() },
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = publicacion.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
@Composable
fun ExpandableDescription(description: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!expanded && textLayoutResult.hasVisualOverflow) {
                    isOverflowing = true
                }
            }
        )

        if (isOverflowing || expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (expanded) "Leer menos" else "Leer más",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(vertical = 2.dp)
            )
        }
    }
}