plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "fr.accentweb.speedMath"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.accentweb.speedMath"
        minSdk = 24
        targetSdk = 36
        versionCode = 11
        versionName = "0.1.11"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.gridlayout:gridlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.navigation:navigation-fragment:2.9.8")
    implementation("androidx.navigation:navigation-ui:2.9.8")
    implementation("com.google.firebase:firebase-auth:24.2.0")
    implementation("com.google.firebase:firebase-config:23.1.0")
    implementation("androidx.work:work-runtime:2.11.2")
    implementation("com.google.firebase:firebase-messaging:25.1.1")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    
    implementation(platform("com.google.firebase:firebase-bom:34.16.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("com.google.firebase:firebase-inappmessaging-display")
}
