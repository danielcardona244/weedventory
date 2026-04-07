package com.example.weedventory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weedventory.ui.screens.HomeScreen
import com.example.weedventory.ui.screens.ProductoScreen
import com.example.weedventory.ui.screens.InventarioScreen
import com.example.weedventory.ui.screens.VentaScreen
import com.example.weedventory.ui.screens.ConsignacionScreen
import com.example.weedventory.ui.screens.PendientesScreen
import com.example.weedventory.ui.screens.HistorialScreen
import com.example.weedventory.ui.viewmodel.ProductoViewModel
import com.example.weedventory.ui.viewmodel.InventarioViewModel
import com.example.weedventory.ui.viewmodel.VentaViewModel
import com.example.weedventory.ui.viewmodel.ConsignacionViewModel

object NavDestinations {
    const val HOME = "home"
    const val PRODUCTOS = "productos"
    const val INVENTARIO = "inventario"
    const val VENTAS = "ventas"
    const val CONSIGNACIONES = "consignaciones"
    const val PENDIENTES = "pendientes"
    const val HISTORIAL = "historial"
}

@Composable
fun WeedventoryNavGraph(
    navController: NavHostController,
    productoViewModel: ProductoViewModel,
    inventarioViewModel: InventarioViewModel,
    ventaViewModel: VentaViewModel,
    consignacionViewModel: ConsignacionViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavDestinations.HOME
    ) {
        composable(NavDestinations.HOME) {
            HomeScreen(navController = navController)
        }
        
        composable(NavDestinations.PRODUCTOS) {
            ProductoScreen(
                navController = navController,
                viewModel = productoViewModel
            )
        }
        
        composable(NavDestinations.INVENTARIO) {
            InventarioScreen(
                navController = navController,
                viewModel = inventarioViewModel,
                productoViewModel = productoViewModel
            )
        }
        
        composable(NavDestinations.VENTAS) {
            VentaScreen(
                navController = navController,
                viewModel = ventaViewModel,
                productoViewModel = productoViewModel
            )
        }
        
        composable(NavDestinations.CONSIGNACIONES) {
            ConsignacionScreen(
                navController = navController,
                viewModel = consignacionViewModel,
                productoViewModel = productoViewModel
            )
        }
        
        composable(NavDestinations.PENDIENTES) {
            PendientesScreen(
                navController = navController,
                viewModel = consignacionViewModel
            )
        }
        
        composable(NavDestinations.HISTORIAL) {
            HistorialScreen(
                navController = navController,
                ventaViewModel = ventaViewModel,
                consignacionViewModel = consignacionViewModel,
                inventarioViewModel = inventarioViewModel
            )
        }
    }
}
