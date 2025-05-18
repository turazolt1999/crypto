package com.example.crypto.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class FavoriteCrypto(
    val userId: String = "",
    val cryptoId: String = "",
    val cryptoName: String = "",
    val addedAt: Timestamp = Timestamp.now(),
    @DocumentId var documentId: String = "",
)