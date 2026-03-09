package com.example.cursy.features.course.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import android.net.Uri
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import coil.compose.AsyncImage

private val GreenPrimary = Color(0xFF2ECC71)

data class EditableBlock(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String = "text",
    val content: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    onNavigateBack: () -> Unit,
    onPublish: (title: String, description: String, coverImage: String, blocks: List<EditableBlock>) -> Unit,
    onUploadImage: suspend (File) -> Result<String>,
    onSaveDraft: (title: String, description: String, coverImage: String, blocks: List<EditableBlock>) -> Unit,
    initialTitle: String = "",
    initialDescription: String = "",
    initialCoverImage: String = "",
    initialBlocks: List<EditableBlock> = emptyList(),
    isEditing: Boolean = false
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var coverImage by remember { mutableStateOf(initialCoverImage) }
    var blocks by remember { mutableStateOf(initialBlocks.ifEmpty { listOf(EditableBlock()) }) }
    var showBlockTypeDialog by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }
    var isProcessingImage by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    
    var activeBlockIndex by remember { mutableStateOf<Int?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isProcessingImage = true
            scope.launch {
                val file = uriToFile(context, it)
                if (file != null) {
                    val result = onUploadImage(file)
                    result.onSuccess { url ->
                        val index = activeBlockIndex
                        if (index != null && index < blocks.size) {
                            // Actualizar bloque de contenido
                            blocks = blocks.toMutableList().also { list ->
                                list[index] = list[index].copy(content = url)
                            }
                            Log.d("CreateCourse", "Media subida para bloque $index: $url")
                        } else {
                            // Actualizar portada
                            coverImage = url
                            Log.d("CreateCourse", "Portada subida: $url")
                        }
                        android.widget.Toast.makeText(context, "Archivo subido correctamente", android.widget.Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        Log.e("CreateCourse", "Error subiendo archivo: ${e.message}")
                        android.widget.Toast.makeText(context, "Error al subir: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("CreateCourse", "Error al procesar archivo local")
                    android.widget.Toast.makeText(context, "Error al procesar archivo seleccionado", android.widget.Toast.LENGTH_SHORT).show()
                }
                isProcessingImage = false
                activeBlockIndex = null // Reset
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEditing) "Editar Curso" else "Nuevo Curso",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onSaveDraft(title, description, coverImage, blocks) }
                    ) {
                        Text("Borrador", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = {
                        isPublishing = true
                        onPublish(title, description, coverImage, blocks)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary
                    ),
                    enabled = title.isNotBlank() && description.isNotBlank() && !isPublishing
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Publish, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEditing) "Guardar Cambios" else "Publicar Curso")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Imagen de Portada",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clickable { 
                            activeBlockIndex = null
                            imagePickerLauncher.launch("image/*") 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (coverImage.isNotEmpty()) {
                        AsyncImage(
                            model = coverImage,
                            contentDescription = "Portada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = { coverImage = "" },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close, 
                                contentDescription = "Quitar imagen",
                                tint = Color.White
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Toca para agregar imagen",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    if (isProcessingImage && activeBlockIndex == null) {
                         Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GreenPrimary)
                        }
                    }
                }
            }


            item {
                Text(
                    "Título del Curso",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Introducción a la Economía") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }


            item {
                Text(
                    "Descripción",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Describe de qué trata tu curso...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Contenido del Curso",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(
                        onClick = { showBlockTypeDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar bloque",
                            tint = GreenPrimary
                        )
                    }
                }
            }

            itemsIndexed(blocks) { index, block ->
                ContentBlockEditor(
                    block = block,
                    onContentChange = { newContent ->
                        blocks = blocks.toMutableList().also {
                            it[index] = block.copy(content = newContent)
                        }
                    },
                    onDelete = {
                        if (blocks.size > 1) {
                            blocks = blocks.toMutableList().also { it.removeAt(index) }
                        }
                    },
                    onMoveUp = {
                        if (index > 0) {
                            blocks = blocks.toMutableList().also {
                                val temp = it[index]
                                it[index] = it[index - 1]
                                it[index - 1] = temp
                            }
                        }
                    },
                    onMoveDown = {
                        if (index < blocks.size - 1) {
                            blocks = blocks.toMutableList().also {
                                val temp = it[index]
                                it[index] = it[index + 1]
                                it[index + 1] = temp
                            }
                        }
                    },
                    onPickMedia = { mimeType ->
                        activeBlockIndex = index
                        imagePickerLauncher.launch(mimeType)
                    },
                    isUploading = isProcessingImage && activeBlockIndex == index
                )
            }
        }
    }

    if (showBlockTypeDialog) {
        AlertDialog(
            onDismissRequest = { showBlockTypeDialog = false },
            title = { Text("Agregar Bloque") },
            text = {
                Column {
                    BlockTypeOption("Encabezado", Icons.Default.Title) {
                        blocks = blocks + EditableBlock(type = "header")
                        showBlockTypeDialog = false
                    }
                    BlockTypeOption("Texto", Icons.Default.TextFields) {
                        blocks = blocks + EditableBlock(type = "text")
                        showBlockTypeDialog = false
                    }
                    BlockTypeOption("Imagen", Icons.Default.Image) {
                        blocks = blocks + EditableBlock(type = "image")
                        showBlockTypeDialog = false
                    }
                    BlockTypeOption("Video", Icons.Default.VideoLibrary) {
                        blocks = blocks + EditableBlock(type = "video")
                        showBlockTypeDialog = false
                    }
                    BlockTypeOption("Código", Icons.Default.Code) {
                        blocks = blocks + EditableBlock(type = "code")
                        showBlockTypeDialog = false
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBlockTypeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun BlockTypeOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = GreenPrimary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 16.sp)
    }
}

@Composable
private fun ContentBlockEditor(
    block: EditableBlock,
    onContentChange: (String) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onPickMedia: (String) -> Unit = {},
    isUploading: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (block.type) {
                        "header" -> "📌 Encabezado"
                        "text" -> "📝 Texto"
                        "image" -> "🖼️ Imagen"
                        "video" -> "🎬 Video"
                        "code" -> "💻 Código"
                        else -> "📝 Texto"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Subir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Bajar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (block.type) {
                "image", "video" -> {
                    if (block.content.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable {
                                    val mimeType = if (block.type == "video") "video/*" else "image/*"
                                    onPickMedia(mimeType)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(color = GreenPrimary)
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        if (block.type == "image") Icons.Default.Image else Icons.Default.VideoLibrary,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (block.type == "image") "Subir Imagen" else "Subir Video",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                        ) {
                            if (block.type == "image") {
                                AsyncImage(
                                    model = block.content,
                                    contentDescription = "Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                var isPrepared by remember { mutableStateOf(false) }

                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    AndroidView(
                                        modifier = Modifier.fillMaxSize(),
                                        factory = { context ->
                                            android.widget.VideoView(context).apply {
                                                setMediaController(android.widget.MediaController(context))
                                                setOnErrorListener { mp, what, extra ->
                                                    android.util.Log.e("VideoPlayer", "Error loading video: what=$what, extra=$extra")
                                                    android.widget.Toast.makeText(context, "Error al reproducir video", android.widget.Toast.LENGTH_SHORT).show()
                                                    true // Handled
                                                }
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
                                                    android.util.Log.e("VideoPlayer", "Exception loading video: ${e.message}")
                                                }
                                            }
                                        }
                                    )
                                    
                                    if (!isPrepared) {
                                        CircularProgressIndicator(color = GreenPrimary)
                                    }
                                }
                            }


                            IconButton(
                                onClick = { onContentChange("") },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                else -> {
                    OutlinedTextField(
                        value = block.content,
                        onValueChange = onContentChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = if (block.type == "header") 56.dp else 100.dp),
                        placeholder = { 
                            Text(
                                when (block.type) {
                                    "header" -> "Título de la sección..."
                                    "code" -> "Escribe tu código aquí..."
                                    else -> "Escribe el contenido..."
                                }
                            ) 
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    }
}


suspend fun uriToFile(context: Context, uri: Uri): File? {
    return withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            val extension = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload", ".$extension", context.cacheDir)
            tempFile.deleteOnExit()
            
            val outputStream = java.io.FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            

            if (mimeType?.startsWith("image/") == true) {
                 val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                 if (bitmap != null) {
                     val maxDimension = 1024
                     val scale = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                         if (bitmap.width > bitmap.height) maxDimension.toFloat() / bitmap.width
                         else maxDimension.toFloat() / bitmap.height
                     } else 1f
                     
                     if (scale != 1f) {
                         val scaledBitmap = Bitmap.createScaledBitmap(
                             bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true
                         )
                         val compressedStream = java.io.FileOutputStream(tempFile) // Sobrescribir
                         scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, compressedStream)
                         compressedStream.close()
                     }
                 }
            }

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
