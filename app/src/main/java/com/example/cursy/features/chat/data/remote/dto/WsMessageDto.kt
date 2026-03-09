package com.example.cursy.features.chat.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WsMessageDto(
    @SerializedName("type") val type: String? = "chat",
    @SerializedName("conversation_id") val conversationId: String? = null,
    @SerializedName("receiver_id") val receiverId: String? = null,
    @SerializedName("content") val content: String = "",
    @SerializedName("sender_id") val senderId: String? = null
)
