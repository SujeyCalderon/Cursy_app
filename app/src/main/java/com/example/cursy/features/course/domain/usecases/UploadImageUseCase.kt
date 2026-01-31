package com.example.cursy.features.course.domain.usecases

import com.example.cursy.features.course.domain.repository.CourseRepository
import java.io.File

class UploadImageUseCase(private val repository: CourseRepository) {
    suspend operator fun invoke(file: File): Result<String> {
        return repository.uploadImage(file)
    }
}
