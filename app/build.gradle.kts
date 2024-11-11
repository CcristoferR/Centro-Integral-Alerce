plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Agrega aquí el plugin de Google Services
}

android {
    namespace = "com.gestionactividades.centrointegralalerce"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gestionactividades.centrointegralalerce"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase BoM para manejar versiones automáticamente
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    // Agrega Firebase Analytics, puedes añadir más servicios Firebase con esta misma configuración
    implementation("com.google.firebase:firebase-analytics")

    // Material CalendarView
    implementation("com.prolificinteractive:material-calendarview:1.4.3")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.google.firebase:firebase-database:20.0.5") // Firebase Realtime Database
    implementation ("com.google.firebase:firebase-storage:20.0.0") // Firebase Storage (para archivos)
    implementation ("com.google.firebase:firebase-auth:21.0.1") // Firebase Auth (para autenticación)

}