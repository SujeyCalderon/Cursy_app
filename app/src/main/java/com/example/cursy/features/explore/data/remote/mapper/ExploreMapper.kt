package com.example.cursy.features.explore.data.remote.mapper

import com.example.cursy.features.explore.data.remote.dto.UserItemDto
import com.example.cursy.features.explore.domain.entities.UserItem

fun UserItemDto.toDomain(): UserItem {
    return UserItem(
        id           = id,
        name         = name,
        bio          = bio ?: "",
        profileImage = profileImage ?: "",
        university   = university ?: ""
    )
}