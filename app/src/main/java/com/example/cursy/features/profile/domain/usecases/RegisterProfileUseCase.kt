package com.example.cursy.features.profile.domain.usecases

import com.example.cursy.features.profile.domain.repository.ProfileRepository

class RegisterProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        ine_url: String,
        university: String? = null
    ): Result<Unit>{
        return repository.registerProfile(name, email, password, ine_url, university)
    }
}