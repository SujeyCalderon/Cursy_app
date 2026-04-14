package com.example.cursy.features.feed.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.feed.data.local.DownloadEntity
import com.example.cursy.features.feed.data.repositories.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// david: ViewModel para la pantalla de descargas
@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val repository: DownloadRepository
) : ViewModel() {

    // david: Exponiendo el flujo de descargas desde Room
    val downloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteDownload(courseId: String) {
        viewModelScope.launch {
            repository.deleteDownload(courseId)
        }
    }
}
