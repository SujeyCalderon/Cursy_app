package com.example.cursy.features.profile.presentation.screens

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.cursy.features.profile.presentation.viewmodels.EditProfileViewModel
import java.io.File
import kotlinx.coroutines.launch

private val GreenPrimary = Color(0xFF2ECC71)

fun Context.findActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    initialProfileImage: String,
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val name            by viewModel.name.collectAsStateWithLifecycle()
    val bio             by viewModel.bio.collectAsStateWithLifecycle()
    val university      by viewModel.university.collectAsStateWithLifecycle()
    val profileImageUrl by viewModel.profileImageUrl.collectAsStateWithLifecycle()
    val isLoading       by viewModel.isLoading.collectAsStateWithLifecycle()
    val isUploading     by viewModel.isUploading.collectAsStateWithLifecycle()
    val saveSuccess     by viewModel.saveSuccess.collectAsStateWithLifecycle()
    val huellaEnabled   by viewModel.huellaEnabled.collectAsStateWithLifecycle()
    val huellaMessage   by viewModel.huellaMessage.collectAsStateWithLifecycle()
    val uploadState     by viewModel.uploadState.collectAsStateWithLifecycle() // NUEVO

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSheet by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // NUEVO: Mostrar mensajes de estado de subida
    LaunchedEffect(uploadState) {
        when (val state = uploadState) {
            is EditProfileViewModel.UploadState.Success -> {
                snackbarHostState.showSnackbar("¡Foto actualizada correctamente!")
            }
            is EditProfileViewModel.UploadState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.onSaveHandled()
            onNavigateBack()
        }
    }

    LaunchedEffect(huellaMessage) {
        if (huellaMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(huellaMessage)
            viewModel.onHuellaMessageHandled()
        }
    }

    fun processSelectedImage(uri: Uri) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "profile_upload_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }
        viewModel.onImageSelected(tempFile)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processSelectedImage(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { processSelectedImage(it) }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val photoFile = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            val photoFile = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(permission)
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Cambiar foto de perfil",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (viewModel.hasCamera) {
                    OutlinedButton(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showSheet = false
                                launchCamera()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GreenPrimary)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tomar foto", color = GreenPrimary)
                    }
                }

                OutlinedButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showSheet = false
                            galleryLauncher.launch("image/*")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GreenPrimary)
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elegir de galería", color = GreenPrimary)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { if (!isUploading) showSheet = true }
            ) {
                AsyncImage(
                    model = profileImageUrl.ifEmpty {
                        "https://via.placeholder.com/120/2ecc71/FFFFFF?text=${name.firstOrNull() ?: 'U'}"
                    },
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(3.dp, GreenPrimary, CircleShape),
                    contentScale = ContentScale.Crop
                )

                // NUEVO: Indicador de estado mejorado
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .background(
                            when (uploadState) {
                                is EditProfileViewModel.UploadState.Success -> Color(0xFF4CAF50)
                                is EditProfileViewModel.UploadState.Error -> Color(0xFFE53935)
                                else -> GreenPrimary
                            },
                            CircleShape
                        )
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isUploading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        }
                        uploadState is EditProfileViewModel.UploadState.Success -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Subida completada",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        uploadState is EditProfileViewModel.UploadState.Error -> {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = "Error de subida",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        else -> {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // NUEVO: Texto de estado debajo de la foto
            Text(
                text = when (uploadState) {
                    is EditProfileViewModel.UploadState.Uploading -> "Subiendo foto..."
                    is EditProfileViewModel.UploadState.Success -> "¡Foto actualizada!"
                    is EditProfileViewModel.UploadState.Error -> "Error al subir. Reintentando..."
                    else -> "Toca para cambiar la foto"
                },
                fontSize = 13.sp,
                color = when (uploadState) {
                    is EditProfileViewModel.UploadState.Success -> Color(0xFF4CAF50)
                    is EditProfileViewModel.UploadState.Error -> Color(0xFFE53935)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, focusedLabelColor = GreenPrimary),
                singleLine = true,
                enabled = !isUploading // NUEVO: Deshabilitar durante subida
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = university,
                onValueChange = viewModel::onUniversityChange,
                label = { Text("Universidad") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, focusedLabelColor = GreenPrimary),
                singleLine = true,
                enabled = !isUploading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = viewModel::onBioChange,
                label = { Text("Biografía") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, focusedLabelColor = GreenPrimary),
                maxLines = 4,
                enabled = !isUploading
            )

            Spacer(modifier = Modifier.weight(1f))

            if (viewModel.isBiometricAvailable) {
                OutlinedButton(
                    onClick = {
                        activity?.let {
                            if (huellaEnabled) viewModel.desactivarHuella()
                            else viewModel.activarHuella(it)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (huellaEnabled) Color.Red else GreenPrimary),
                    enabled = !isUploading
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = if (huellaEnabled) Color.Red else GreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (huellaEnabled) "Desactivar huella" else "Activar inicio con huella",
                        color = if (huellaEnabled) Color.Red else GreenPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = { viewModel.saveProfile(initialProfileImage) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = !isLoading && !isUploading
            ) {
                if (isLoading || isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Guardar Cambios", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}