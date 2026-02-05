package com.example.cursy.features.Register.presenstation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.cursy.R

@Composable
fun FormRegister(){
    var nombre by remember { mutableStateOf("") } //se cambiara a viewModel
    var password by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
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
                "Unete a la red universitaria de intercambio",
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
                onValueChange = { nombre = it },
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
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { Text("Escribe tu nombre") },
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
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { Text("Minimo 8 caracteres") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                visualTransformation = if (password)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val icon = if (password)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff

                    IconButton(onClick = { password = !password }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            )


        }
    }

}


@Preview(showBackground = true)
@Composable
fun showForm(){
    FormRegister()
}