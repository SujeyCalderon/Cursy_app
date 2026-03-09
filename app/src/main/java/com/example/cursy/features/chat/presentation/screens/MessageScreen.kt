package com.example.cursy.features.chat.presentation.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
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
import coil.compose.AsyncImage
import com.example.cursy.features.chat.domain.entities.Message
import com.example.cursy.features.chat.domain.entities.MessageStatus
import com.example.cursy.features.chat.presentation.viewmodels.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    viewModel: ChatViewModel,
    conversationId: String,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }

    LaunchedEffect(uiState.currentMessages) {
        if (uiState.currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.currentMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            val otherUser = uiState.currentConversation
            TopAppBar(
                title = {
                    val otherUserId = otherUser?.otherUserId ?: ""
                    val isOnline = uiState.userStatuses[otherUserId] ?: false

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            name = otherUser?.otherUserName ?: "Chat",
                            imageUrl = otherUser?.otherUserImage ?: "",
                            size = 36
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = otherUser?.otherUserName ?: "Mensajes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = if (isOnline) "En línea" else "Desconectado",
                                color = if (isOnline) Color(0xFF00D186) else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                reverseLayout = false
            ) {
                items(uiState.currentMessages) { message ->
                    val isMine = message.senderId == uiState.currentUserId
                    val otherUser = uiState.currentConversation
                    val myProfile = uiState.myProfile

                    MessageBubble(
                        message = message,
                        isMine = isMine,
                        userName = if (isMine) (myProfile?.name ?: "") else (otherUser?.otherUserName ?: ""),
                        userImage = if (isMine) (myProfile?.profileImage ?: "") else (otherUser?.otherUserImage ?: "")
                    )
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(conversationId, messageText)
                            messageText = ""
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF00D186))
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isMine: Boolean,
    userName: String,
    userImage: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMine) {
            UserAvatar(name = userName, imageUrl = userImage)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Surface(
                color = when {
                    message.status == MessageStatus.FAILED -> Color(0xFFFFCDD2)
                    message.status == MessageStatus.PENDING -> if (isMine) Color(0xFF80E8B6) else Color(0xFFF0F0F0)
                    isMine -> Color(0xFF00D186)
                    else -> Color(0xFFF0F0F0)
                },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMine) 16.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 16.dp
                )
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = when {
                        message.status == MessageStatus.FAILED -> Color(0xFFB71C1C)
                        isMine -> Color.White
                        else -> Color.Black
                    },
                    fontSize = 15.sp
                )
            }


            if (isMine && message.status != MessageStatus.SENT) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    when (message.status) {
                        MessageStatus.PENDING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enviando...", fontSize = 10.sp, color = Color.Gray)
                        }
                        MessageStatus.FAILED -> {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Error",
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFFB71C1C)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Error al enviar", fontSize = 10.sp, color = Color(0xFFB71C1C))
                        }
                        else -> {}
                    }
                }
            }
        }

        if (isMine) {
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatar(name = userName, imageUrl = userImage)
        }
    }
}

@Composable
fun UserAvatar(name: String, imageUrl: String, size: Int = 32) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = name.take(1).uppercase(),
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = (size / 2).sp
            )
        }
    }
}
