// Versiones compartidas para todo el proyecto
extra.apply {
    set("kotlin_version", "2.0.0")
    set("compileSdk", 34)
    set("minSdk", 26)
    set("targetSdk", 34)
    set("versionCode", 1)
    set("versionName", "1.0.0")
    
    // Jetpack & Androidx
    set("composeVersion", "1.6.0")
    set("composeMaterial3", "1.1.1")
    set("navigationCompose", "2.7.5")
    set("lifecycleVersion", "2.6.2")
    set("coreKtxVersion", "1.12.0")
    set("appcompatVersion", "1.6.1")
    
    // Room
    set("roomVersion", "2.6.0")
    
    // WorkManager
    set("workManagerVersion", "2.8.1")
    
    // JSON Serialization
    set("kotlinxSerializationVersion", "1.6.0")
    
    // Testing
    set("junitVersion", "4.13.2")
    set("junitComposeVersion", "1.5.4")
    set("espressoVersion", "3.5.1")
}
