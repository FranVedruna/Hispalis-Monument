plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.hispalismonumentapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.hispalismonumentapp"
        minSdk = 25
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.jakewharton.picasso:picasso2-okhttp3-downloader:1.1.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation ("com.google.guava:guava:31.1-android")
    implementation ("com.github.bumptech.glide:glide:4.15.1")


    // Para el mapa (OpenStreetMap)
    implementation ("org.osmdroid:osmdroid-android:6.1.16")

// Para calcular rutas (OSRM)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    testImplementation(libs.ext.junit)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.generativeai)
    implementation(libs.firebase.crashlytics.buildtools)
    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.mockito:mockito-core:3.12.4")
    testImplementation ("org.mockito:mockito-inline:3.12.4")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation ("org.mockito:mockito-inline:3.12.4'")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}