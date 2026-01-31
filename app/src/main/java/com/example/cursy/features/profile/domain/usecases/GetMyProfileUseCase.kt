package com.example.cursy.features.profile.domain.usecases

import com.example.cursy.features.profile.domain.entities.Profile
import com.example.cursy.features.profile.domain.repository.ProfileRepository

class GetMyProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): Result<Profile> {
        return repository.getMyProfile()
    }
}