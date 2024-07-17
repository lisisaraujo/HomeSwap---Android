import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    alias(libs.plugins.google.gms.google.services)
}

// api keys
val amadeusClientID: String = gradleLocalProperties(rootDir, providers).getProperty("amadeusClientID")
val amadeusClientSecret: String = gradleLocalProperties(rootDir, providers).getProperty("amadeusClientSecret")
val googlePlacesApiKey: String = gradleLocalProperties(rootDir, providers).getProperty("googlePlacesApiKey")

android {
    namespace = "com.example.homeswap_android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.homeswap_android"
        minSdk = 24
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
            buildConfigField("String", "amadeusClientID", amadeusClientID)
            buildConfigField("String", "amadeusClientSecret", amadeusClientSecret)
            buildConfigField("String", "googlePlacesApiKey", googlePlacesApiKey)

        }
        debug {
            buildConfigField("String", "amadeusClientID", amadeusClientID)
            buildConfigField("String", "amadeusClientSecret", amadeusClientSecret)
            buildConfigField("String", "googlePlacesApiKey", googlePlacesApiKey)
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.2.0")


    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi.converter)
    implementation(libs.moshi.kotlin)

    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)

//     Room Dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0")

    implementation ("com.google.firebase:firebase-appcheck-playintegrity:17.0.1")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))

    // Add the dependencies for the App Check libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-appcheck-debug")

    // circle image view
    implementation ("de.hdodenhof:circleimageview:3.1.0")


    // google places api
    implementation ("com.google.android.libraries.places:places:3.1.0")

    implementation ("com.google.android.material:material:1.9.0")
}


