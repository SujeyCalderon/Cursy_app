package com.example.cursy.features.course.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import coil.compose.AsyncImage
import com.example.cursy.components.PublishFirstDialog
import com.example.cursy.features.course.domain.entities.ContentBlock
import com.example.cursy.features.course.domain.entities.ContentBlockType
import com.example.cursy.features.course.presentation.viewmodels.CourseDetailViewModel

private val GreenPrimary = Color(0xFF2ECC71)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    viewModel: CourseDetailViewModel,
    onNavigateBack: () -> Unit,
    onEditCourse: () -> Unit = {},
    onCreateCourse: () -> Unit = {},
    hasPublishedCourse: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(courseId) {
        viewModel.loadCourse(courseId)
    }

    // Si se eliminó exitosamente, navega atrás
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onNavigateBack()
        }
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
                    val isPublishFirstError = uiState.error?.contains("publish", ignoreCase = true) == true ||
                            uiState.error?.contains("publicar", ignoreCase = true) == true
                    
                    if (isPublishFirstError) {
                        PublishFirstDialog(
                            onDismiss = onNavigateBack,
                            onCreateCourse = {
                                onNavigateBack()
                                onCreateCourse()
                            }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "😕",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error ?: "Error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadCourse(courseId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenPrimary
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                uiState.course != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            AsyncImage(
                                model = uiState.course!!.coverImage,
                                contentDescription = uiState.course!!.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = uiState.course!!.title,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Por: ${uiState.course!!.authorName}",
                                        fontSize = 16.sp,
                                        color = Color(0xFF5F6368)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Divider()

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "DESCRIPCIÓN",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = uiState.course!!.description,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Divider()

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "CONTENIDO DEL CURSO",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        items(uiState.course!!.blocks) { block ->
                            ContentBlockItem(block)
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
                
                else -> {
                     Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
            }
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red
                )
            },
            title = { Text("Eliminar curso") },
            text = {
                Text("¿Estás seguro de que deseas eliminar este curso? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteCourse(courseId) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("Cancelar")
                }
            }
        )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        ContentBlockType.TEXT -> {
            Text(
                text = block.content,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        ContentBlockType.IMAGE -> {
            AsyncImage(
                model = block.content,
                contentDescription = "Imagen del curso",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
        }

        ContentBlockType.VIDEO -> {
            // Video Player
            var isPrepared by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        android.widget.VideoView(context).apply {
                            setMediaController(android.widget.MediaController(context))
                        }
                    },
                    update = { videoView ->
                        if (videoView.tag != block.content) {
                             try {
                                val uri = android.net.Uri.parse(block.content)
                                videoView.setVideoURI(uri)
                                videoView.tag = block.content
                                isPrepared = false
                                
                                videoView.setOnPreparedListener { mp ->
                                    mp.seekTo(1)
                                    isPrepared = true

                                    val viewWidth = videoView.width.toFloat()
                                    val viewHeight = videoView.height.toFloat()
                                    if (viewWidth > 0 && viewHeight > 0 && mp.videoWidth > 0 && mp.videoHeight > 0) {
                                        val videoRatio = mp.videoWidth.toFloat() / mp.videoHeight.toFloat()
                                        val viewRatio = viewWidth / viewHeight
                                        
                                        if (videoRatio > viewRatio) {
                                            videoView.scaleX = videoRatio / viewRatio
                                        } else {
                                            videoView.scaleY = viewRatio / videoRatio
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )

                if (!isPrepared) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
        }

        ContentBlockType.CODE -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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

    Spacer(modifier = Modifier.height(16.dp))
}