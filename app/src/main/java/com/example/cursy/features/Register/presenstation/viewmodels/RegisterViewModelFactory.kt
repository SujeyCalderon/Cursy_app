package com.example.cursy.features.Register.presenstation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cursy.features.profile.domain.usecases.RegisterProfileUseCase

class RegisterViewModelFactory(
    private val registerProfileUseCase: RegisterProfileUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(registerProfileUseCase) as T
    }
}
