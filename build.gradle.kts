plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.rgbmems_smartphoneapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rgbmems_smartphoneapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    viewBinding {
        enable = true  // Đúng cú pháp
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
