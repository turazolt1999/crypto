package com.example.crypto.pages

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.size.Size
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.crypto.AuthState
import com.example.crypto.AuthViewModel
import com.example.crypto.api.CryptoData
import com.example.crypto.api.CryptoViewModel
import coil.compose.AsyncImagePainter

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    cryptoViewModel: CryptoViewModel
) {
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
                    CryptoItem(crypto = crypto)
                    HorizontalDivider()
                }
            }
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

@Composable
fun MenuItem(
    icon: ImageVector,
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text)
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
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(crypto.image)
                .size(Size.ORIGINAL)
                .build()
        )

        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is AsyncImagePainter.State.Error -> {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Hiba a képbetöltéskor",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                Image(
                    painter = painter,
                    contentDescription = "${crypto.name} logo",
                    modifier = Modifier.size(40.dp)
                )
            }
        }

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