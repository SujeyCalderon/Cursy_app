package com.example.cursy.features.profile.domain.usecases

import com.example.cursy.features.profile.domain.repository.ProfileRepository

class UpdateProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(
        name: String? = null,
        profileImage: String? = null,
        bio: String? = null,
        university: String? = null
    ): Result<Unit> {
        return repository.updateProfile(name, profileImage, bio, university)
    }
}
