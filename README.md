# Weedventory - App Android de Gestión de Inventario

Aplicación Android nativa para control de inventario, ventas y consignaciones offline.

## Requisitos

- Android SDK 34 (Android 14)
- Gradle 8.2.0
- Kotlin 2.0.0
- Teléfono Android con minSDK 24 (Android 7.0)
- AS tener Android Studio instalado

## Stack Tecnológico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **BD Local:** Room (SQLite)
- **Arquitectura:** MVVM
- **Navegación:** Navigation Compose
- **ViewModels:** Lifecycle ViewModel + StateFlow
- **Workers:** WorkManager (recordatorios)
- **Notificaciones:** NotificationManager
- **Serialización:** Kotlin Serialization

## Estructura del Proyecto

```
app/
├── src/main/
│   ├── java/com/example/weedventory/
│   │   ├── data/
│   │   │   ├── local/db/
│   │   │   │   ├── entity/ (Entidades Room)
│   │   │   │   ├── dao/ (DAOs)
│   │   │   │   └── AppDatabase.kt
│   │   │   ├── repository/ (Repositorios con lógica)
│   │   │   └── utils/ (Datos de ejemplo)
│   │   ├── domain/
│   │   │   ├── model/ (Tipos y enems)
│   │   │   └── usecase/ (Casos de uso)
│   │   ├── ui/
│   │   │   ├── navigation/ (NavGraph)
│   │   │   ├── screens/ (Pantallas)
│   │   │   ├── components/ (Componentes Compose)
│   │   │   ├── theme/ (Tema Material 3)
│   │   │   └── viewmodel/ (ViewModels)
│   │   ├── worker/ (WorkManager)
│   │   ├── notification/ (Notificaciones)
│   │   ├── utils/ (Utilidades)
│   │   └── MainActivity.kt
│   └── res/ (Recursos)
├── build.gradle.kts
```

## Pasos para Compilar y Ejecutar

### 1. Clonar/Preparar Proyecto

```bash
# Navegar al directorio del proyecto
cd c:\Users\cardo\Projects\Weedventory
```

### 2. Sincronizar Gradle

Abre el proyecto en Android Studio y espera a que sincronice Gradle automáticamente.

Si hay errores, ejecuta:
```bash
./gradlew clean build
```

### 3. Conectar Dispositivo Android

**Opción A: Teléfono físico**
- Conecta tu teléfono por USB
- Habilita "Depuración USB" en Opciones de Desarrollador
- Verifica la conexión:
  ```bash
  adb devices
  ```

**Opción B: Emulador**
- Abre Android Studio → Device Manager
- Crea o inicia un emulador

### 4. Ejecutar la App

En Android Studio:
1. Haz clic en "Run" (botón verde de play) o presiona `Shift + F10`
2. Selecciona el dispositivo donde ejecutar
3. Espera a que compile e instale

O desde terminal:
```bash
./gradlew installDebug
adb shell am start -n com.example.weedventory/.MainActivity
```

## Características Principales

### 1. Gestión de Productos
- Crear, editar, ver productos
- Registro de stock (actual, mínimo)
- Alertas de stock bajo
- Precios de costo y venta

### 2. Control de Inventario
- Registrar entradas de stock
- Registrar salidas (venta, consumo propio)
- Ajustes manuales
- Historial completo de movimientos

### 3. Registro de Ventas
- Ventas normales (con cliente opcional)
- Consumo propio del negocio
- Múltiples items por venta
- Descuento automático de stock

### 4. Gestión de Consignaciones
- Registrar consignaciones
- Asignar fecha de recordatorio
- Seguimiento de saldo pendiente
- Estados: PENDIENTE, SALDADA, VENCIDA
- Registro de pagos parciales

### 5. Notificaciones
- Recordatorios automáticos de consignaciones
- Se ejecuta diariamente vía WorkManager
- Notificaciones locales en dispositivo

### 6. Historial
- Registro completo de ventas
- Historial de consignaciones
- Historial de movimientos de inventario

## Entidades de Base de Datos

### Producto
- id (PK)
- nombre, descripción
- costo, precioVenta
- stockActual, stockMinimo
- activo, fechaCreacion

### MovimientoInventario
- id (PK)
- productoId (FK)
- tipoMovimiento (ENTRADA, VENTA, CONSUMO, AJUSTE, CONSIGNACION)
- cantidad, fecha
- referenciaId, nota

### Venta
- id (PK)
- fecha, tipoVenta (NORMAL, CONSUMO_PROPIO)
- cliente (opcional)
- total, observacion

### DetalleVenta
- id (PK)
- ventaId (FK), productoId (FK)
- cantidad, precioUnitario, subtotal

### Consignacion
- id (PK)
- comprador
- fechaEntrega, fechaRecordatorio
- montoTotal, saldoPendiente
- estado (PENDIENTE, SALDADA, VENCIDA)
- observaciones, fechaCreacion

### ConsignacionDetalle
- id (PK)
- consignacionId (FK), productoId (FK)
- cantidad, precioUnitario, subtotal

## Datos de Ejemplo

La app incluye un generador de datos de ejemplo en `SampleDataGenerator.kt`. 
Contiene 5 productos predefinidos para testing:
- Cannabis Sativa
- Cannabis Indica
- Aceite CBD
- Tinctura
- Edibles

## Reglas de Negocio Implementadas

✅ No permitir vender más stock del disponible
✅ No permitir consignar más stock del disponible
✅ Descuento automático de stock al registrar venta/consignación
✅ Incremento de stock al registrar entrada
✅ Descuento de stock al registrar consumo propio
✅ Recordatorios locales en fechas elegidas
✅ Estados de consignación actualizando automáticamente
✅ Seguimiento de saldo pendiente

## Permisos Necesarios

```xml
<!-- En AndroidManifest.xml -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

Nota: En Android 13+ se solicita permiso al ejecutar por primera vez.

## Navegación

La app tiene las siguientes pantallas:

1. **Home** - Menú principal
2. **Productos** - Gestión de productos
3. **Inventario** - Registrar entradas y consultar historial
4. **Ventas** - Registrar ventas normales y consumo propio
5. **Consignaciones** - Registrar consignaciones
6. **Pendientes** - Ver consignaciones por revisar/vencidas
7. **Historial** - Resumen de actividad

## Consignación Automática de Recordatorios

WorkManager ejecuta diariamente:
- Verifica consignaciones con fecha de recordatorio hoy
- Muestra notificación para cada una
- Actualiza estado a VENCIDA si pasó la fecha

## Troubleshooting

### Error de sincronización Gradle
```bash
./gradlew clean
./gradlew build
```

### Error: "Plugin com.google.devtools.ksp not found"
Asegúrate que tu `build.gradle.kts` (raíz) tiene:
```kotlin
id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
```

### Base de datos no se inicializa
Verifica que AppDatabase está correctamente instanciado en MainActivity.

### Notificaciones no funcionan
- Verifica que el permiso POST_NOTIFICATIONS está concedido
- En test, abre la app y espera a que WorkManager ejecute

## Desarrollo Futuro

Sugerencias de expansión:
- Exportar datos a CSV/Excel
- Gráficos de ventas/inventario
- Búsqueda y filtros avanzados
- Backup automático
- Sincronización nube opcional (Firebase)
- Acceso multi-usuario
- Reportes detallados

## Licencia

Proyecto personal.

## Autor

Desarrollado para uso personal offline.
