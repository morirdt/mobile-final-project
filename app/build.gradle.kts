plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.example.mobilefinalproject"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.mobilefinalproject"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Resolve MAPS_API_KEY from multiple places (project property, local.properties, env var)
        val mapsApiKeyFromProject = (project.findProperty("MAPS_API_KEY") as String?)
        val mapsApiKeyFromEnv = System.getenv("MAPS_API_KEY")
        val mapsApiKeyFromLocalProperties = run {
            val lp = rootProject.file("local.properties")
            if (lp.exists()) {
                try {
                    val text = lp.readText(Charsets.UTF_8)
                    // Match lines like: MAPS_API_KEY=AIzaSy...
                    val regex = Regex("^MAPS_API_KEY=(.*)$", RegexOption.MULTILINE)
                    val m = regex.find(text)
                    m?.groups?.get(1)?.value?.trim()
                } catch (e: Exception) {
                    logger.warn("Failed to read local.properties for MAPS_API_KEY: ${e.message}")
                    null
                }
            } else null
        }
        val mapsApiKey = mapsApiKeyFromProject ?: mapsApiKeyFromLocalProperties ?: mapsApiKeyFromEnv ?: ""

        // Debugging: log resolved key during Gradle configuration (use logger so it appears with --info)
        logger.lifecycle("[build.gradle.kts] Resolved MAPS_API_KEY='${mapsApiKey}' (from project? ${mapsApiKeyFromProject != null}, localProps? ${mapsApiKeyFromLocalProperties != null}, env? ${mapsApiKeyFromEnv != null})")

        if (mapsApiKey.isBlank()) {
            throw GradleException("MAPS_API_KEY is not set. Set it in local.properties (MAPS_API_KEY=...), as a project property (-PMAPS_API_KEY=...), or as an environment variable MAPS_API_KEY. Build cannot continue without it.")
        }

        // Expose to BuildConfig for runtime access in code
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")

        // Also pass into AndroidManifest via manifest placeholders (for Google Maps API meta-data)
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation("com.google.android.libraries.places:places:3.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.squareup.picasso:picasso:2.8")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}