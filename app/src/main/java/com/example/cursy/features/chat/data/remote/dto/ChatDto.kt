package com.example.cursy.features.chat.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ConversationDto(
    @SerializedName("id") val id: String,
    @SerializedName("participants") val participants: List<String>? = null,
    @SerializedName("other_user") val otherUser: OtherUserDto? = null,
    @SerializedName("last_message") val lastMessage: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class OtherUserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("profile_image") val profileImage: String
)

data class MessageDto(
    @SerializedName("id") val id: String,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val updatedAt: String
)
