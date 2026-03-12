package com.example.cursy.features.Review.Presentation.Screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.cursy.features.Review.Presentation.CommentUiModel  // <- aquí el import correcto
import com.example.cursy.features.Review.Presentation.Viewmodels.ReviewsViewModel
import kotlinx.coroutines.launch

private val GreenPrimary = Color(0xFF2ECC71)
private val GreenLight   = Color(0xFFE8F8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    courseId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: ReviewsViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()

    val sheetState        = rememberModalBottomSheetState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSheet by remember { mutableStateOf(false) }
    val scope     = rememberCoroutineScope()

    LaunchedEffect(courseId) {
        viewModel.loadComments(courseId)
    }

    LaunchedEffect(uiState.commentSent) {
        if (uiState.commentSent) {
            scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
            viewModel.onCommentSentHandled()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.error)
            viewModel.onErrorHandled()
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Escribe tu comentario",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                OutlinedTextField(
                    value = uiState.commentText,
                    onValueChange = viewModel::onCommentTextChange,
                    placeholder = { Text("¿Qué opinas del curso?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor  = GreenPrimary
                    ),
                    maxLines = 5
                )

                Button(
                    onClick = { viewModel.sendComment(courseId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = !uiState.isSending && uiState.commentText.isNotBlank()
                ) {
                    if (uiState.isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publicar comentario", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Comentarios", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escribir un comentario", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }

            uiState.comments.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💬", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Aún no hay comentarios",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "¡Sé el primero en comentar!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${uiState.comments.size} comentario${if (uiState.comments.size != 1) "s" else ""}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(uiState.comments) { comment ->
                        CommentCard(comment = comment)
                    }
                }
            }
        }
    }
}

@Composable
fun CommentCard(comment: CommentUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = comment.userImage.ifEmpty {
                        "https://via.placeholder.com/40/2ecc71/FFFFFF?text=${comment.userName.firstOrNull() ?: 'U'}"
                    },
                    contentDescription = "Foto de ${comment.userName}",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GreenLight)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = comment.userName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = comment.createdAt,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(GreenPrimary, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = comment.content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

//scroll
// Versión embebida sin Scaffold ni LazyColumn — para usar dentro de CourseDetailScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmbeddedReviews(
    courseId: String,
    viewModel: ReviewsViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState        = rememberModalBottomSheetState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSheet by remember { mutableStateOf(false) }
    val scope     = rememberCoroutineScope()

    LaunchedEffect(courseId) {
        viewModel.loadComments(courseId)
    }

    LaunchedEffect(uiState.commentSent) {
        if (uiState.commentSent) {
            scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
            viewModel.onCommentSentHandled()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.error)
            viewModel.onErrorHandled()
        }
    }

    // Botton
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Escribe tu comentario", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                OutlinedTextField(
                    value = uiState.commentText,
                    onValueChange = viewModel::onCommentTextChange,
                    placeholder = { Text("¿Qué opinas del curso?") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor  = GreenPrimary
                    ),
                    maxLines = 5
                )

                Button(
                    onClick = { viewModel.sendComment(courseId) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = !uiState.isSending && uiState.commentText.isNotBlank()
                ) {
                    if (uiState.isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publicar comentario", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    //
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

        SnackbarHost(snackbarHostState)

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }

            uiState.comments.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💬", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Aún no hay comentarios",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "¡Sé el primero en comentar!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                Text(
                    text = "${uiState.comments.size} comentario${if (uiState.comments.size != 1) "s" else ""}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                uiState.comments.forEach { comment ->
                    CommentCard(comment = comment)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón
        OutlinedButton(
            onClick = { showSheet = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GreenPrimary)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Escribir un comentario", color = GreenPrimary, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewsScreenPreview() {
    ReviewsScreen(courseId = "preview")
}