package com.example.crypto.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import com.example.crypto.components.CryptoListItems
import com.example.crypto.components.MenuItem

@Composable
fun FavoritesPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    cryptoViewModel: CryptoViewModel,
    isGuest: Boolean
) {
    var selectedCrypto by remember { mutableStateOf<CryptoData?>(null) }

    val authState = authViewModel.authState.observeAsState()
    val cryptoList = cryptoViewModel.cryptoList
    val isLoading = cryptoViewModel.isLoading.value
    val error = cryptoViewModel.error.value
    val favorites by cryptoViewModel.favorites.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf("favorites") }
    var isGuest = remember { mutableStateOf(isGuest) }

    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("recent") }
    var showSortOptions by remember { mutableStateOf(false) }

    LaunchedEffect(authState.value, isGuest, sortOption) {
        if (isGuest.value) {
            cryptoViewModel.clearFavorites()
        } else {
            when (authState.value) {
                is AuthState.Authenticated -> {
                    when (sortOption) {
                        "name" -> cryptoViewModel.searchFavoritesByName(searchQuery)
                        else -> cryptoViewModel.loadFavorites()
                    }
                }
                is AuthState.Unauthenticated -> {
                    navController.navigate("login")
                }
                else -> Unit
            }
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
            Text(text = "Favorites", fontSize = 32.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        cryptoViewModel.searchFavoritesByName(it)
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Search favorites") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            error?.let {
                Text(text = it, color = Color.Red, modifier = Modifier.padding(16.dp))
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                val favoriteCryptos = cryptoList.filter { crypto -> favorites.contains(crypto.id) }

                items(favoriteCryptos) { crypto ->
                    CryptoListItems(
                        crypto = crypto,
                        isFavorite = true,
                        onFavoriteClick = {
                            if (!isGuest.value) {
                                cryptoViewModel.toggleFavorite(crypto.id, crypto.name)
                            } else {
                                navController.navigate("login")
                            }
                        },
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
                                navController.popBackStack("home", inclusive = false)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        if (isGuest.value) {
                            MenuItem(
                                icon = Icons.Default.AccountCircle,
                                text = "Sign In",
                                onClick = {
                                    authViewModel.signout(cryptoViewModel)
                                    navController.navigate("login") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = true
                                        }
                                    }
                                    showMenu = false
                                }
                            )
                        } else {
                            MenuItem(
                                icon = Icons.Default.Favorite,
                                text = "Favorites",
                                selected = currentRoute == "favorites",
                                onClick = {
                                    currentRoute = "favorites"
                                    showMenu = false
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            MenuItem(
                                icon = Icons.Default.AccountCircle,
                                text = "Logout",
                                onClick = {
                                    authViewModel.signout(cryptoViewModel)
                                    showMenu = false
                                }
                            )
                        }
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
