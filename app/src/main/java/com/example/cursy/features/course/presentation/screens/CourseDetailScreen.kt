package com.example.cursy.features.course.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.cursy.LocalDetailScreenState
import com.example.cursy.LocalVideoService
import com.example.cursy.features.Review.Presentation.Screen.EmbeddedReviews
import com.example.cursy.features.course.domain.entities.ContentBlock
import com.example.cursy.features.course.domain.entities.ContentBlockType
import com.example.cursy.features.course.presentation.viewmodels.CourseDetailViewModel
import com.example.cursy.features.feed.data.local.DownloadStatus

private val GreenPrimary = Color(0xFF2ECC71)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    viewModel: CourseDetailViewModel,
    onNavigateBack: () -> Unit,
    onEditCourse: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onCreateCourse: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") hasPublishedCourse: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val setDetailScreenState = LocalDetailScreenState.current

    // Notificar al MainActivity que estamos en la pantalla de detalle
    DisposableEffect(Unit) {
        setDetailScreenState(true)
        onDispose {
            setDetailScreenState(false)
        }
    }

    LaunchedEffect(courseId) {
        viewModel.loadCourse(courseId)
    }

    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onNavigateBack()
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text("Eliminar Curso") },
            text = { Text("¿Estás seguro de que deseas eliminar este curso? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCourse(courseId) }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (uiState.isOwner) {
                        IconButton(onClick = { viewModel.showMenu() }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                        DropdownMenu(
                            expanded = uiState.showMenu,
                            onDismissRequest = { viewModel.hideMenu() }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = {
                                    viewModel.hideMenu()
                                    onEditCourse()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = Color.Red) },
                                onClick = {
                                    viewModel.hideMenu()
                                    viewModel.showDeleteDialog()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                }
                            )
                        }
                    } else {
                        IconButton(onClick = { viewModel.toggleSave() }) {
                            Icon(
                                imageVector = if (uiState.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = if (uiState.isSaved) "Quitar de guardados" else "Guardar curso",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4285F4),
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "😕", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = uiState.error ?: "Error", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadCourse(courseId) }, colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) {
                            Text("Reintentar")
                        }
                    }
                }

                uiState.course != null -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            AsyncImage(
                                model = uiState.course!!.coverImage,
                                contentDescription = uiState.course!!.title,
                                modifier = Modifier.fillMaxWidth().height(240.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(text = uiState.course!!.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Por: ${uiState.course!!.authorName}", fontSize = 16.sp, color = Color(0xFF5F6368))
                                
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DownloadButton(
                                        status = uiState.downloadStatus,
                                        progress = uiState.downloadProgress,
                                        onDownloadClick = { viewModel.startCourseDownload() },
                                        onDeleteClick = { viewModel.deleteDownload(courseId) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "DESCRIPCIÓN", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = uiState.course!!.description, fontSize = 16.sp, lineHeight = 24.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "CONTENIDO DEL CURSO", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        items(uiState.course!!.blocks) { block ->
                            ContentBlockItem(block)
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "COMENTARIOS", fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            EmbeddedReviews(courseId = courseId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadButton(
    status: DownloadStatus,
    progress: Int,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (status) {
        DownloadStatus.PENDING, DownloadStatus.FAILED -> {
            Button(
                onClick = onDownloadClick,
                modifier = modifier,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (status == DownloadStatus.FAILED) "Reintentar descarga" else "Descargar curso")
            }
        }
        DownloadStatus.DOWNLOADING -> {
            Column(modifier = modifier) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                    color = GreenPrimary
                )
                Text(
                    text = "Descargando... $progress%",
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        DownloadStatus.COMPLETED -> {
            OutlinedButton(
                onClick = onDeleteClick,
                modifier = modifier,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Eliminar descarga")
            }
        }
    }
}

@Composable
fun ContentBlockItem(block: ContentBlock) {
    when (block.type) {
        ContentBlockType.HEADER -> {
            Text(
                text = block.content,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        ContentBlockType.TEXT -> {
            Text(
                text = block.content,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        ContentBlockType.IMAGE -> {
            AsyncImage(
                model = block.content,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
        }
        ContentBlockType.VIDEO -> {
            VideoPlayer(url = block.content)
        }
        ContentBlockType.CODE -> {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))
            ) {
                Text(
                    text = block.content,
                    fontSize = 14.sp,
                    color = Color(0xFF80CBC4),
                    modifier = Modifier.padding(16.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun VideoPlayer(url: String) {
    val videoService = LocalVideoService.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (videoService != null) {
            val player = videoService.getPlayer()
            if (player != null) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = true
                            setBackgroundColor(android.graphics.Color.BLACK)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { _ ->
                        val currentMediaUri = player.currentMediaItem?.localConfiguration?.uri?.toString()
                        if (currentMediaUri != url) {
                            videoService.playVideo(url)
                        }
                    }
                )
            } else {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            CircularProgressIndicator(color = GreenPrimary)
        }
    }
}
