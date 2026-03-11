package com.example.cursy.features.login.Local.Mapper

import com.example.cursy.features.login.Local.Entities.BiometricEntity
import com.example.cursy.features.profile.domain.entities.Biometric

fun BiometricEntity.toDomain(): Biometric{
    return Biometric(
        userId = userId,
        keyUser = keyUser,
        tokenLogin = tokenLogin,
        stateHuella = stateHuella
    )
}

fun Biometric.toEntity(): BiometricEntity{
    return BiometricEntity(
        userId = userId,
        keyUser = keyUser,
        tokenLogin = tokenLogin,
        stateHuella = stateHuella
    )
}