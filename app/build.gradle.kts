plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.parcelize")
    kotlin("plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.mikepenz.aboutlibraries.plugin")
}

adjustFlavorTasks()

kotlin {
    jvmToolchain(21)
}

android {

    namespace = "ua.syt0r.kanji"

    compileSdk = 36
    defaultConfig {
        applicationId = "ua.syt0r.kanji"
        minSdk = 26
        targetSdk = 36
        versionCode = AppVersion.versionCode
        versionName = AppVersion.versionName
    }

    buildTypes {
        val debug = getByName("debug") {
            versionNameSuffix = "-debug"
            applicationIdSuffix = ".dev"
        }

        val release = getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions += "version"

    productFlavors {
        create("googlePlay") {
            dimension = "version"
        }

        create("fdroid") {
            dimension = "version"
            applicationIdSuffix = ".fdroid"
        }
    }

    buildFeatures {
        compose = true
    }

    val keystoreFile = rootProject.file("keystore.jks")

    val signedBuildSigningConfig = signingConfigs.create("signedBuild") {
        storeFile = keystoreFile
        System.getenv("KEYSTORE_PASS")?.let { storePassword = it }
        System.getenv("SIGN_KEY")?.let { keyAlias = it }
        System.getenv("SIGN_PASS")?.let { keyPassword = it }
    }

    val debugSigningConfig = signingConfigs.getByName("debug")

    buildTypes.forEach {
        it.signingConfig = if (keystoreFile.exists()) {
            signedBuildSigningConfig
        } else {
            debugSigningConfig
        }
    }

    dependenciesInfo {
        // Removes a signing block with encrypted data for reproducible F-Droid builds
        includeInApk = false
    }

}

dependencies {
    implementation(project(":core"))

    "googlePlayImplementation"(platform(libs.firebase.bom))
    "googlePlayImplementation"(libs.firebase.analytics.ktx)
    "googlePlayImplementation"(libs.firebase.crashlytics.ktx)
    "googlePlayImplementation"(libs.billing.ktx)
    "googlePlayImplementation"(libs.review.ktx)
}

aboutLibraries {
    configPath = "core/credits"
    excludeFields = arrayOf("generated")
}

fun adjustFlavorTasks() {
    val hasGoogleServices = project.file("google-services.json").exists() ||
        project.file("src/googlePlay/google-services.json").exists()

    project.gradle.taskGraph.whenReady {
        allTasks.forEach { task ->

            val isFdroid = task.name.contains("fdroid", ignoreCase = true)

            val isGoogleTask = task.name.contains("GoogleServices", ignoreCase = true) ||
                    task.name.contains("Crashlytics", ignoreCase = true)

            val isArtProfileTask = task.name.contains("ArtProfile", ignoreCase = true)

            if ((isFdroid && (isGoogleTask || isArtProfileTask)) || (!hasGoogleServices && isGoogleTask)) {
                println("Disabling task: ${task.name}")
                task.enabled = false
            }

        }
    }

    val copyPreviewDebugApk = tasks.register<Copy>("copyPreviewDebugApk") {
        from(layout.buildDirectory.file("outputs/apk/fdroid/debug/app-fdroid-debug.apk"))
        into(layout.buildDirectory.dir("outputs/apk/debug"))
        rename("app-fdroid-debug.apk", "app-debug.apk")
    }

    tasks.matching { it.name == "assembleDebug" || it.name == "assembleFdroidDebug" }.configureEach {
        finalizedBy(copyPreviewDebugApk)
    }
}
