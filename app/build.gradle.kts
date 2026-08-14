import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing is configured out-of-tree. Point at a keystore either with a
// `keystore.properties` file at the repo root (gitignored) or with environment
// variables. Nothing secret is ever read from, or written to, version control.
//
//   keystore.properties:
//     storeFile=/absolute/path/to/sanskrit-keyboards-release.jks
//     storePassword=...
//     keyAlias=sanskrit
//     keyPassword=...
//
//   or: SANSKRIT_KEYSTORE_FILE / SANSKRIT_KEYSTORE_PASSWORD
//       SANSKRIT_KEY_ALIAS / SANSKRIT_KEY_PASSWORD
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

fun secret(propKey: String, envKey: String): String? =
    keystoreProps.getProperty(propKey) ?: System.getenv(envKey)

val releaseStoreFile = secret("storeFile", "SANSKRIT_KEYSTORE_FILE")
val releaseStorePassword = secret("storePassword", "SANSKRIT_KEYSTORE_PASSWORD")
val releaseKeyAlias = secret("keyAlias", "SANSKRIT_KEY_ALIAS")
val releaseKeyPassword = secret("keyPassword", "SANSKRIT_KEY_PASSWORD")
val hasReleaseSigning = releaseStoreFile != null && file(releaseStoreFile).exists()

android {
    namespace = "com.thermetery.sanskritkeyboards"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.thermetery.sanskritkeyboards"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.1"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // Test-only. The shipped APK carries no dependencies at all.
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test:core:1.6.1")
}
