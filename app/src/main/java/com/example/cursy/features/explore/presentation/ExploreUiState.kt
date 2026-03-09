package com.example.cursy.features.explore.presentation

import com.example.cursy.features.explore.domain.entities.UserItem

data class ExploreUiState(
    val isLoading: Boolean = false,
    val users: List<UserItem> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
) {
    val filteredUsers: List<UserItem>
        get() = if (searchQuery.isBlank()) users
        else users.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.bio.contains(searchQuery, ignoreCase = true)
        }
}