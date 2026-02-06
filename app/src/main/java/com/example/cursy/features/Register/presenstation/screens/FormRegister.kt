package com.example.cursy.features.Register.presenstation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cursy.R
import com.example.cursy.core.di.AppContainer
import com.example.cursy.features.Register.presenstation.viewmodels.RegisterViewModel
import com.example.cursy.features.Register.presenstation.viewmodels.RegisterViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box

@Composable
fun FormRegister(
    appContainer: AppContainer,
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel(
factory = RegisterViewModelFactory(appContainer.registerProfileUseCase)
)){
    val nombre by viewModel.name.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val passwordVisible by viewModel.passwordVisible.collectAsStateWithLifecycle()

    val message by viewModel.message.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show snackbar when error changes
    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            snackbarHostState.showSnackbar(error)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFE74C3C),
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(30.dp)
                .verticalScroll(rememberScrollState())
        ) {

        IconButton(onClick = { /* acción */ }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Logo + Cursy
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.icongreen),
                contentDescription = "school",
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                "Cursy",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5159F9)
            )
        }

        Column(modifier = Modifier.padding(top = 20.dp)) {
            Text(
                "Crea tu cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "Únete a la red universitaria de intercambio",
                fontSize = 17.sp
            )

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "Nombre completo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = viewModel::onNameChange,
                placeholder = { Text("Escribe tu nombre") },
                modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Correo electronico",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = viewModel::onEmailChange,
                placeholder = { Text("usuario@gmail.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Contraseña",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(10.dp))


            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = { Text("Minimo 8 caracteres") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val icon = if (passwordVisible)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff

                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            )

            //el cuadro de fotografia
            Spacer(modifier = Modifier.height(25.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(
                        width = 2.dp,
                        color = Color(0xFFA6A6A6),
                        shape = RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Visibility, // cámbialo por Photo si quieres
                        contentDescription = "Agregar foto",
                        tint = Color(0xFF5159F9),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Agregar fotografía",
                        color = Color(0xFF5159F9),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }



            Button(
                modifier = Modifier.fillMaxWidth().padding(top= 20.dp),
                onClick = {
                    viewModel.onRegister()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2ECC71),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Regístrate")
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = Color(0xFF2ECC71),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
                // Trigger navigation to login after successful registration
                androidx.compose.runtime.LaunchedEffect(message) {
                    if (message.isNotEmpty()) {
                        kotlinx.coroutines.delay(1500)
                        onRegisterSuccess()
                    }
                }
            }

            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

        }

        Spacer(modifier = Modifier.height(40.dp))

        // Already have account - navigate to login (moved outside inner Column)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¿Ya tienes cuenta? ",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Inicia sesión",
                fontSize = 14.sp,
                color = Color(0xFF2ECC71),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
    }
}



@Preview(showBackground = true)
@Composable
fun showForm(){
    FormRegister(appContainer = AppContainer())
}