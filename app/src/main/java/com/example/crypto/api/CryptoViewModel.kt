package com.example.crypto.api

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.State


class CryptoViewModel : ViewModel() {
    private val _cryptoList = mutableStateListOf<CryptoData>()
    val cryptoList: List<CryptoData> = _cryptoList

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error
    init {
        loadCryptoData()
    }

    fun loadCryptoData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = ApiClient.cryptoApiService.getCryptoData()
                _cryptoList.clear()
                _cryptoList.addAll(response)
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}