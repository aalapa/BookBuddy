import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// Read version code from properties file
val versionPropertiesFile = rootProject.file("version.properties")
val versionProperties = Properties()
if (versionPropertiesFile.exists()) {
    versionProperties.load(FileInputStream(versionPropertiesFile))
}
var versionCodeValue = (versionProperties.getProperty("VERSION_CODE") ?: "1").toInt()

// Auto-increment version code
versionCodeValue++
versionProperties.setProperty("VERSION_CODE", versionCodeValue.toString())
versionProperties.store(FileOutputStream(versionPropertiesFile), "Auto-incremented version code")

// Calculate version name (major.minor.patch) from incremented version code
val majorVersion = 1
val minorVersion = 0
val patchVersion = versionCodeValue
val versionNameValue = "$majorVersion.$minorVersion.$patchVersion"

// Print version info for debugging
println("Building with versionCode: $versionCodeValue, versionName: $versionNameValue")

android {
    namespace = "com.bookbuddy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bookbuddy"
        minSdk = 24
        targetSdk = 34
        versionCode = versionCodeValue
        versionName = versionNameValue

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            // Use debug signing config for release (for development/testing only)
            // In production, you should create a proper release keystore
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
    
    // Customize APK output name
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val buildType = variant.buildType.name
            val versionName = variant.versionName
            val versionCode = variant.versionCode
            
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName = 
                "BookBuddy-${buildType}-v${versionName}(${versionCode}).apk"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

