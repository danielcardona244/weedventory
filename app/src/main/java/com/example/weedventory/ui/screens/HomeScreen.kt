package com.example.weedventory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.weedventory.ui.navigation.NavDestinations

@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weedventory") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Gestión de Inventario",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MenuButton(
                title = "Productos",
                description = "Crear y gestionar productos",
                onClick = { navController.navigate(NavDestinations.PRODUCTOS) }
            )
            
            MenuButton(
                title = "Inventario",
                description = "Registrar entradas y ajustes de stock",
                onClick = { navController.navigate(NavDestinations.INVENTARIO) }
            )
            
            MenuButton(
                title = "Ventas",
                description = "Registrar ventas normales y consumo propio",
                onClick = { navController.navigate(NavDestinations.VENTAS) }
            )
            
            MenuButton(
                title = "Consignaciones",
                description = "Registrar nuevas consignaciones",
                onClick = { navController.navigate(NavDestinations.CONSIGNACIONES) }
            )
            
            MenuButton(
                title = "Pendientes",
                description = "Ver consignaciones pendientes por revisar",
                onClick = { navController.navigate(NavDestinations.PENDIENTES) }
            )
            
            MenuButton(
                title = "Historial",
                description = "Ver historial de ventas, consignaciones e inventario",
                onClick = { navController.navigate(NavDestinations.HISTORIAL) }
            )
        }
    }
}

@Composable
fun MenuButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
