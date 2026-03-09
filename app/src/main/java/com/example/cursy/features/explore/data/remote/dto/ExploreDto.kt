package com.example.cursy.features.explore.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UsersResponse(
    val users: List<UserItemDto>
)

data class UserItemDto(
    val id: String,
    val name: String,
    val email: String,                    // necesario para ChatRepositoryImpl.searchUsers()
    val bio: String?,
    @SerializedName("profile_image")
    val profileImage: String?,
    val university: String?
)