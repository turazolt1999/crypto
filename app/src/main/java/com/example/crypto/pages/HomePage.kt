package com.example.crypto.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.crypto.AuthState
import com.example.crypto.AuthViewModel
import com.example.crypto.api.CryptoData
import com.example.crypto.api.CryptoViewModel
import com.example.crypto.components.CryptoDetailsDialog
import com.example.crypto.components.CryptoItem
import com.example.crypto.components.MenuItem

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    cryptoViewModel: CryptoViewModel
) {
    var selectedCrypto by remember { mutableStateOf<CryptoData?>(null) }

    val authState = authViewModel.authState.observeAsState()
    val cryptoList = cryptoViewModel.cryptoList
    val isLoading = cryptoViewModel.isLoading.value
    val error = cryptoViewModel.error.value

    var showMenu by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf("home") }

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Crypto", fontSize = 32.sp)

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            error?.let {
                Text(text = it, color = Color.Red, modifier = Modifier.padding(16.dp))
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cryptoList) { crypto ->
                    CryptoItem(
                        crypto = crypto,
                        onClick = { selectedCrypto = crypto }
                    )
                    HorizontalDivider()
                }
            }
        }

        selectedCrypto?.let { crypto ->
            CryptoDetailsDialog(
                crypto = crypto,
                onDismiss = { selectedCrypto = null }
            )
        }

        FloatingActionButton(
            onClick = { showMenu = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }

        if (showMenu) {
            AlertDialog(
                onDismissRequest = { showMenu = false },
                title = { Text("Menu") },
                text = {
                    Column {
                        MenuItem(
                            icon = Icons.Default.Home,
                            text = "Home",
                            selected = currentRoute == "home",
                            onClick = {
                                currentRoute = "home"
                                showMenu = false
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        MenuItem(
                            icon = Icons.Default.AccountCircle,
                            text = "Logout",
                            onClick = {
                                authViewModel.signout()
                                showMenu = false
                            }
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showMenu = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}