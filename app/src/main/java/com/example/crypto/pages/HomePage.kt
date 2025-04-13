package com.example.crypto.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.crypto.AuthState
import com.example.crypto.AuthViewModel
import com.example.crypto.api.CryptoData
import com.example.crypto.api.CryptoViewModel

@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, cryptoViewModel: CryptoViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val cryptoList = cryptoViewModel.cryptoList
    val isLoading = cryptoViewModel.isLoading.value
    val error = cryptoViewModel.error.value

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "CryptoWorld", fontSize = 32.sp)

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        error?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(16.dp))
        }

        LazyColumn {
            items(cryptoList) { crypto ->
                CryptoItem(crypto = crypto)
                Divider()
            }
        }


        TextButton(onClick = {
            authViewModel.signout()
        }) {
            Text(text = "Sign out")
        }
    }

}

@Composable
fun CryptoItem(crypto: CryptoData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = crypto.image,
            contentDescription = "${crypto.name} logo",
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = crypto.name, fontWeight = FontWeight.Bold)
            Text(text = crypto.symbol.uppercase(), color = Color.Gray)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = "$${"%.2f".format(crypto.current_price)}")
            Text(
                text = "${"%.2f".format(crypto.price_change_percentage_24h)}%",
                color = if (crypto.price_change_percentage_24h >= 0) Color.Green else Color.Red
            )
        }
    }
}