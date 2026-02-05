package com.example.cursy.features.login.domain.usecases

import com.example.cursy.core.network.LoginResponse
import com.example.cursy.features.profile.domain.repository.ProfileRepository

class LoginUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(email: String, password: String): Result<LoginResponse> {
        return repository.login(email, password)
    }
}
