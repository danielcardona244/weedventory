package com.example.weedventory.data.local.db.entity

/**
 * Enumeración de tipos de movimiento en inventario
 */
enum class TipoMovimiento {
    ENTRADA,           // Ingreso de nuevo stock
    VENTA,             // Venta normal
    CONSUMO,           // Consumo propio
    AJUSTE,            // Ajuste manual
    CONSIGNACION       // Entrega en consignación
}

/**
 * Enumeración de estados de consignación
 */
enum class EstadoConsignacion {
    PENDIENTE,         // Aún no saldada
    SALDADA,           // Cliente pagó
    VENCIDA            // Pasó la fecha de recordatorio sin ser saldada
}

/**
 * Enumeración de tipos de venta
 */
enum class TipoVenta {
    NORMAL,            // Venta normal de mostrador
    CONSUMO_PROPIO     // Consumo propio del negocio
}
