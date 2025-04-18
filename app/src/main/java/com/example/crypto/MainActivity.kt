package com.example.crypto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.crypto.api.CryptoViewModel
import com.example.crypto.ui.theme.CryptoTheme
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

class MainActivity : ComponentActivity() {
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analytics = Firebase.analytics
        val bundle = Bundle().apply {
            putString("status", "app_started")
        }
        analytics.logEvent("app_launch", bundle)

        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        val cryptoViewModel : CryptoViewModel by viewModels()
        setContent {
            CryptoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(modifier = Modifier.padding(innerPadding), authViewModel = authViewModel, cryptoViewModel = cryptoViewModel)
                }
            }
        }
    }
}