@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.weedventory.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.weedventory.R
import com.example.weedventory.ui.viewmodel.ConsignacionViewModel
import com.example.weedventory.ui.viewmodel.ProductoViewModel
import com.example.weedventory.ui.viewmodel.VentaViewModel

sealed class HomeTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Inventario : HomeTab("Inventario", Icons.Default.List)
    object Venta : HomeTab("Venta", Icons.Default.ShoppingCart)
    object Historial : HomeTab("Historial", Icons.Default.History)
}

@Composable
fun HomeScreen(
    navController: NavHostController,
    productoViewModel: ProductoViewModel,
    ventaViewModel: VentaViewModel,
    consignacionViewModel: ConsignacionViewModel
) {
    var selectedTab by remember { mutableStateOf<HomeTab>(HomeTab.Inventario) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_wdty),
                            contentDescription = "Logo Weedventory",
                            modifier = Modifier.size(40.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Weedventory",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Inventario, ventas y consignaciones",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                listOf(HomeTab.Inventario, HomeTab.Venta, HomeTab.Historial).forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { androidx.compose.material3.Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                is HomeTab.Inventario -> InventarioScreen(
                    productoViewModel = productoViewModel
                )
                is HomeTab.Venta -> VentaScreen(
                    viewModel = ventaViewModel,
                    consignacionViewModel = consignacionViewModel,
                    productoViewModel = productoViewModel
                )
                is HomeTab.Historial -> HistorialScreen(
                    ventaViewModel = ventaViewModel,
                    consignacionViewModel = consignacionViewModel
                )
            }
        }
    }
}
