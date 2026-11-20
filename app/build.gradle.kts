plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val authBaseUrl = providers.gradleProperty("authBaseUrl")
val operationsBaseUrl = providers.gradleProperty("operationsBaseUrl")

android {
    namespace = "com.example.electromechanics"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.electromechanics"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "AUTH_BASE_URL",
            "\"${authBaseUrl.getOrElse("https://electromechanics-auth.onrender.com/")}\"")
        buildConfigField("String", "OPERATIONS_BASE_URL",
            "\"${operationsBaseUrl.getOrElse("http://10.0.2.2:8082/")}\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "AUTH_BASE_URL",
                "\"${authBaseUrl.getOrElse("http://10.0.2.2:8081/")}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("demo") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            applicationIdSuffix = ".demo"
            buildConfigField("String", "AUTH_BASE_URL", "\"http://127.0.0.1:8081/\"")
            buildConfigField("String", "OPERATIONS_BASE_URL", "\"http://127.0.0.1:8082/\"")
        }
    }
    sourceSets.getByName("demo") {
        manifest.srcFile("src/debug/AndroidManifest.xml")
        res.srcDir("src/debug/res")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // GPS (ubicación)
    implementation(libs.play.services.location)

    // Navigation (Usando la sintaxis del Version Catalog)
    implementation(libs.androidx.navigation.compose)

    // Red: Retrofit + OkHttp (cliente HTTP) + Gson (JSON)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
