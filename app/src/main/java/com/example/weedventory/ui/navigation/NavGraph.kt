package com.example.weedventory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weedventory.ui.screens.HistorialScreen
import com.example.weedventory.ui.screens.HomeScreen
import com.example.weedventory.ui.screens.InventarioScreen
import com.example.weedventory.ui.screens.VentaScreen
import com.example.weedventory.ui.viewmodel.ConsignacionViewModel
import com.example.weedventory.ui.viewmodel.ProductoViewModel
import com.example.weedventory.ui.viewmodel.VentaViewModel

object NavDestinations {
    const val HOME = "home"
}

@Composable
fun WeedventoryNavGraph(
    navController: NavHostController,
    productoViewModel: ProductoViewModel,
    ventaViewModel: VentaViewModel,
    consignacionViewModel: ConsignacionViewModel,
    historialRequestId: Int = 0
) {
    NavHost(
        navController = navController,
        startDestination = NavDestinations.HOME
    ) {
        composable(NavDestinations.HOME) {
            HomeScreen(
                navController = navController,
                productoViewModel = productoViewModel,
                ventaViewModel = ventaViewModel,
                consignacionViewModel = consignacionViewModel,
                historialRequestId = historialRequestId
            )
        }
    }
}
