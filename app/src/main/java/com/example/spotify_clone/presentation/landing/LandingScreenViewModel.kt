package com.example.spotify_clone.presentation.landing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LandingScreenViewModel: ViewModel() {
    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible

    fun updateIsVisible(updatedValue: Boolean) {
        _isVisible.value = updatedValue
    }
}