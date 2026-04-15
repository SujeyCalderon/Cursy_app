package com.example.cursy.features.chat.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WsMessageDto(
    @SerializedName("type") val type: String? = "chat",
    @SerializedName("conversation_id") val conversationId: String? = null,
    @SerializedName("receiver_id") val receiverId: String? = null,
    @SerializedName("content") val content: String = "",
    @SerializedName("sender_id") val senderId: String? = null,
    @SerializedName("author_id") val author_id: String? = null,
    @SerializedName("author_name") val author_name: String? = null,
    @SerializedName("course_id") val courseId: String? = null,
    @SerializedName("course_title") val courseTitle: String? = null,
    @SerializedName("commenter_name") val commenterName: String? = null,
    @SerializedName("target_user") val targetUser: String? = null
)
