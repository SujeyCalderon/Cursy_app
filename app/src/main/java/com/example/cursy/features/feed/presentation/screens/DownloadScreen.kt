package com.example.cursy.features.feed.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cursy.features.feed.data.local.DownloadEntity
import com.example.cursy.features.feed.data.local.DownloadStatus
import com.example.cursy.features.feed.presentation.viewmodels.DownloadViewModel
import java.io.File

private val GreenPrimary = Color(0xFF2ECC71)

// david: Pantalla para visualizar y gestionar cursos descargados para ver offline
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel,
    onNavigateBack: () -> Unit,
    onPlayOffline: (String) -> Unit
) {
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Descargas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4285F4),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (downloads.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("😕", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No tienes cursos descargados", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(downloads) { download ->
                        DownloadItem(
                            download = download,
                            onDelete = { viewModel.deleteDownload(download.courseId) },
                            onPlay = { onPlayOffline(download.localPath ?: "") }
                        )
                    }
                }
            }
        }
    }
}

// david: Item individual de la lista de descargas
@Composable
fun DownloadItem(
    download: DownloadEntity,
    onDelete: () -> Unit,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { if (download.status == DownloadStatus.COMPLETED) onPlay() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(download.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                
                when (download.status) {
                    DownloadStatus.DOWNLOADING -> {
                        LinearProgressIndicator(
                            progress = { download.progress / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = GreenPrimary
                        )
                        Text("Descargando... ${download.progress}%", fontSize = 12.sp, color = Color.Gray)
                    }
                    DownloadStatus.COMPLETED -> {
                        Text("Listo para ver offline", fontSize = 14.sp, color = GreenPrimary)
                    }
                    DownloadStatus.FAILED -> {
                        Text("Error en la descarga", fontSize = 14.sp, color = Color.Red)
                    }
                    else -> {}
                }
            }
            
            if (download.status == DownloadStatus.COMPLETED) {
                IconButton(onClick = onPlay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir", tint = GreenPrimary)
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}
