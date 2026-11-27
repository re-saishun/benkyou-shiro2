plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.parcelize")
    kotlin("plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.mikepenz.aboutlibraries.plugin")
    alias(libs.plugins.secrets)
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
        buildConfig = true
    }

    val keystoreFile = rootProject.file("keystore.jks")

    val signedBuildSigningConfig = signingConfigs.create("signedBuild") {
        storeFile = keystoreFile
        System.getenv("KEYSTORE_PASS")?.let { storePassword = it }
        System.getenv("SIGN_KEY")?.let { keyAlias = it }
        System.getenv("SIGN_PASS")?.let { keyPassword = it }
    }

    val debugKeystoreFile = rootProject.file("debug.keystore")
    val debugSigningConfig = if (debugKeystoreFile.exists()) {
        signingConfigs.create("debugConfig") {
            storeFile = debugKeystoreFile
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    } else {
        signingConfigs.getByName("debug")
    }

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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

}

androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.flavorName == "googlePlay" && variantBuilder.buildType == "debug") {
            variantBuilder.enable = false
        }
    }
}

dependencies {
    implementation(project(":core"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.firebase.appcheck.recaptcha)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    "googlePlayImplementation"(libs.firebase.analytics)
    "googlePlayImplementation"(libs.firebase.crashlytics)
    "googlePlayImplementation"(libs.billing.ktx)
    "googlePlayImplementation"(libs.review.ktx)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")
}

secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
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

            val isGoogleServicesTask = task.name.contains("GoogleServices", ignoreCase = true)
            val isCrashlyticsTask = task.name.contains("Crashlytics", ignoreCase = true)
            val isGoogleTask = isGoogleServicesTask || isCrashlyticsTask

            val isArtProfileTask = task.name.contains("ArtProfile", ignoreCase = true)

            if ((isFdroid && (isCrashlyticsTask || isArtProfileTask)) || (!hasGoogleServices && isGoogleTask)) {
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
