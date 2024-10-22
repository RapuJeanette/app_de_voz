plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.apptopicos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.apptopicos"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0") // Google API Extensions

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("com.google.cloud:google-cloud-dialogflow:2.0.0")
    implementation("com.google.api-client:google-api-client-android:1.32.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.1.0")

    // gRPC OkHttp transport (que ya tienes)
    implementation("io.grpc:grpc-okhttp:1.32.2")
    // gRPC core
    implementation("io.grpc:grpc-core:1.32.2")
    // gRPC stub (necesario para manejar las llamadas a gRPC)
    implementation("io.grpc:grpc-stub:1.32.2")
    // Protobuf (necesario para las clases de diálogo y respuestas de Dialogflow)
    implementation("com.google.protobuf:protobuf-java:3.21.12")
    // gRPC Context
    implementation("io.grpc:grpc-context:1.32.2")

}