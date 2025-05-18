package com.example.crypto.api

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.firestore.FavoriteCrypto
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CryptoViewModel : ViewModel() {
    private val _cryptoList = mutableStateListOf<CryptoData>()
    val cryptoList: List<CryptoData> = _cryptoList

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _favorites = MutableStateFlow<List<String>>(emptyList())
    val favorites: StateFlow<List<String>> = _favorites.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _isLoggingOut = mutableStateOf(false)

    val isLoggingOut: Boolean
        get() = _isLoggingOut.value

    fun setLoggingOut(isLoggingOut: Boolean) {
        _isLoggingOut.value = isLoggingOut
        if (isLoggingOut) {
            _favorites.value = emptyList()
            _error.value = null
        }
    }
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


    fun addFavorite(cryptoId: String, cryptoName: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val favorite = FavoriteCrypto(
                    userId = userId,
                    cryptoId = cryptoId,
                    cryptoName = cryptoName,
                    addedAt = Timestamp.now()
                )

                db.collection("favorites")
                    .add(favorite)
                    .await()

            } catch (e: Exception) {
                _error.value = "Failed to add favorite: ${e.message}"
            }
        }
    }

    fun loadFavorites() {
        val userId = auth.currentUser?.uid ?: run {
            _favorites.value = emptyList()
            return
        }

        if (!isLoggingOut) {
            db.collection("favorites")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshots, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    val newFavorites = mutableListOf<String>()
                    snapshots?.forEach { document ->
                        document.getString("cryptoId")?.let { cryptoId ->
                            newFavorites.add(cryptoId)
                        }
                    }
                    _favorites.value = newFavorites
                }
        }
    }

    fun toggleFavorite(cryptoId: String, cryptoName: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val query = db.collection("favorites")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("cryptoId", cryptoId)
                    .limit(1)
                    .get()
                    .await()

                if (query.documents.isEmpty()) {
                    addFavorite(cryptoId, cryptoName)
                } else {
                    query.documents.first().reference.delete().await()
                }
            } catch (e: Exception) {
                _error.value = "Failed to toggle favorite: ${e.message}"
            }
        }
    }

    fun clearFavorites() {
        _favorites.value = emptyList()
    }
}