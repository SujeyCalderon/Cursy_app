package com.example.cursy.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private val GreenPrimary = Color(0xFF2ECC71)
private val GreenDark = Color(0xFF27AE60)
private val GreenLight = Color(0xFFE8F5E9)

@Composable
fun PublishFirstDialog(
    onDismiss: () -> Unit,
    onCreateCourse: () -> Unit
) {
    // Animaciones
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "entryScale"
    )
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .scale(animatedScale)
                .alpha(animatedAlpha),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono animado con brillo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GreenLight,
                                    GreenLight.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(50.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GreenPrimary, GreenDark)
                                ),
                                shape = RoundedCornerShape(35.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .alpha(shimmerAlpha),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Título con emoji
                Text(
                    text = "✨ ¡Comparte tu conocimiento! ✨",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Descripción
                Text(
                    text = "Para acceder a cursos de otros estudiantes, primero necesitas publicar al menos un curso tuyo.",
                    fontSize = 15.sp,
                    color = Color(0xFF7F8C8D),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mensaje motivacional
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = GreenLight
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🤝 Es un trueque justo: tú compartes, y nosotros compartimos contigo",
                        fontSize = 14.sp,
                        color = GreenDark,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Botón principal con gradiente
                Button(
                    onClick = onCreateCourse,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(GreenPrimary, GreenDark)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "🚀 Crear mi primer curso",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón secundario
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Más tarde",
                        color = Color(0xFF95A5A6),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
