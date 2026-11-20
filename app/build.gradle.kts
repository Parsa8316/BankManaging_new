plugins {

alias(libs.plugins.android.application)

alias(libs.plugins.kotlin.compose)

alias(libs.plugins.kotlin.serialization)

}



android {

namespace = "com.bankmanaging.app"

compileSdk = 36



defaultConfig {

applicationId = "com.bankmanaging.app"

minSdk = 24

targetSdk = 36

versionCode = 2

versionName = "2.0.0"



testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

}



// ۱. کل بلوک signingConfigs را کامنت کنید:

/*

signingConfigs {

create("release") {

val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"

storeFile = file(keystorePath)

storePassword = System.getenv("STORE_PASSWORD")

keyAlias = "upload"

keyPassword = System.getenv("KEY_PASSWORD")

}

create("debugConfig") {

storeFile = file("${rootDir}/debug.keystore")

storePassword = "android"

keyAlias = "androiddebugkey"

keyPassword = "android"

}

}

*/



buildTypes {

release {

isCrunchPngs = false

isMinifyEnabled = false

proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

// ۲. این خط را هم حتماً کامنت کنید (تا خطای release not found ندهد):

// signingConfig = signingConfigs.getByName("release")

}

debug {

// ۳. این خط را هم کامنت کنید:

// signingConfig = signingConfigs.getByName("debugConfig")

}

}



compileOptions {

sourceCompatibility = JavaVersion.VERSION_17

targetCompatibility = JavaVersion.VERSION_17

}



buildFeatures {

compose = true

buildConfig = true

}

}



dependencies {

implementation(libs.androidx.core.ktx)

implementation(libs.androidx.lifecycle.runtime.ktx)

implementation(libs.androidx.lifecycle.runtime.compose)

implementation(libs.androidx.lifecycle.viewmodel.compose)

implementation(libs.androidx.activity.compose)

implementation(platform(libs.androidx.compose.bom))

implementation(libs.androidx.ui)

implementation(libs.androidx.ui.graphics)

implementation(libs.androidx.ui.tooling.preview)

implementation(libs.androidx.material3)

implementation(libs.androidx.material.icons.extended)

implementation(libs.androidx.navigation.compose)

implementation(libs.androidx.datastore.preferences)

implementation(libs.kotlinx.serialization.json)

implementation(libs.kotlinx.coroutines.android)

}
