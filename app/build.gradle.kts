plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.ever_after"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ever_after"
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

    // 🛠 Fix for META-INF/DEPENDENCIES issue
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation ("org.osmdroid:osmdroid-android:6.1.16")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation(libs.androidx.core.ktx)
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.material.v180)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("com.airbnb.android:lottie:5.0.3")
    implementation(libs.androidx.viewpager2)
    implementation(libs.play.services.auth)
    implementation("com.google.firebase:firebase-auth:22.2.0")
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.cardview)
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.14.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation(libs.firebase.messaging.ktx)
    annotationProcessor(libs.compiler)
    implementation(libs.glide)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
