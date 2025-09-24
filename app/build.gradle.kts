import java.io.File

plugins {
    id("com.android.application") version "8.2.2"
    id("org.jetbrains.kotlin.android") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

fun String?.ifNotBlank(): String? = this?.takeIf { it.isNotBlank() }

fun loadEnvFile(file: File): Map<String, String> {
    if (!file.exists()) return emptyMap()
    return file.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { line ->
            val index = line.indexOf('=')
            if (index <= 0) {
                null
            } else {
                val key = line.substring(0, index).trim()
                val value = line.substring(index + 1).trim()
                if (key.isEmpty()) null else key to value
            }
        }
        .toMap()
}
fun resolveSecret(propertyName: String, envKey: String, secrets: Map<String, String>): String {
    return (project.findProperty(propertyName) as? String)?.trim().ifNotBlank()
        ?: secrets[envKey]?.trim().ifNotBlank()
        ?: System.getenv(envKey)?.trim().ifNotBlank()
        ?: ""
}

val runpodSecrets = loadEnvFile(rootProject.file("config/runpod/supabase.env"))

fun String.escapeForBuildConfig(): String =
    this.replace("\\", "\\\\").replace("\"", "\\\"")

val translatorApiBaseUrl =
    resolveSecret("VAVUS_TRANSLATOR_API_BASE_URL", "TRANSLATOR_API_BASE_URL", runpodSecrets)
val supabaseUrl = (project.findProperty("VAVUS_SUPABASE_URL") as? String)?.trim().orEmpty()
val supabaseAnonKey =
    resolveSecret("VAVUS_SUPABASE_ANON_KEY", "SUPABASE_ANON_KEY", runpodSecrets)

android {
    namespace = "com.example.vavusaitranslator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.vavusaitranslator"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    buildConfigField(
        "String",
        "TRANSLATOR_API_BASE_URL",
        "\"${translatorApiBaseUrl.escapeForBuildConfig()}\""
    )
    buildConfigField(
        "String",
        "SUPABASE_URL",
        "\"${supabaseUrl.escapeForBuildConfig()}\""    )
    buildConfigField(
        "String",
        "SUPABASE_ANON_KEY",
        "\"${supabaseAnonKey.escapeForBuildConfig()}\""
    )
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

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.10" }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core + lifecycle
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-graphics") // Color, StrokeCap, etc.
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // View-system Material (for XML theme if referenced)
    implementation("com.google.android.material:material:1.12.0")

    // Data + networking
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    val supabaseBom = platform("io.github.jan-tennert.supabase:bom:2.3.1")
    implementation(supabaseBom)
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
