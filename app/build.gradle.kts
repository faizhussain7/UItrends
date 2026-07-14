@file:Suppress("UnstableApiUsage")

import java.util.Properties

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

fun Properties.creatorValue(propertyKey: String, envKey: String): String {
    System.getenv(envKey)?.takeIf { it.isNotBlank() }?.let { return it.trim() }
    return getProperty(propertyKey)?.trim()
        ?: error(
            "Missing creator config '$propertyKey'. " +
                "Set release/creator.properties or env $envKey.",
        )
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mfhapps.trendingui"
    compileSdk = 37

    val versionPropertiesFile = rootProject.file("release/version.properties")
    val versionProperties = Properties().apply {
        if (versionPropertiesFile.exists()) {
            versionPropertiesFile.inputStream().use { load(it) }
        }
    }
    val releaseVersionCode =
        versionProperties.getProperty("versionCode")?.toIntOrNull() ?: 1
    val releaseVersionName =
        versionProperties.getProperty("versionName") ?: "1.0.0"

    val creatorPropertiesFile = rootProject.file("release/creator.properties")
    val creatorProperties = Properties().apply {
        if (creatorPropertiesFile.exists()) {
            creatorPropertiesFile.inputStream().use { load(it) }
        }
    }

    defaultConfig {
        applicationId = "com.mfhapps.trendingui"
        minSdk = 28
        targetSdk = 37
        versionCode = releaseVersionCode
        versionName = releaseVersionName

        val creatorGithubUsername =
            creatorProperties.creatorValue("githubUsername", "CREATOR_GITHUB_USERNAME")
        val creatorLinkedinHandle =
            creatorProperties.creatorValue("linkedinHandle", "CREATOR_LINKEDIN_HANDLE")
        val creatorLinkedinProfile =
            creatorProperties.creatorValue("linkedinProfile", "CREATOR_LINKEDIN_PROFILE")
        val creatorXHandle =
            creatorProperties.creatorValue("xHandle", "CREATOR_X_HANDLE")
        val creatorEmail =
            creatorProperties.creatorValue("email", "CREATOR_EMAIL")
        val creatorWhatsappIndiaE164 =
            creatorProperties.creatorValue("whatsappIndiaE164", "CREATOR_WHATSAPP_INDIA_E164")
        val creatorWhatsappSaudiE164 =
            creatorProperties.creatorValue("whatsappSaudiE164", "CREATOR_WHATSAPP_SAUDI_E164")
        val creatorWhatsappIndiaDisplay =
            creatorProperties.creatorValue("whatsappIndiaDisplay", "CREATOR_WHATSAPP_INDIA_DISPLAY")
        val creatorWhatsappSaudiDisplay =
            creatorProperties.creatorValue("whatsappSaudiDisplay", "CREATOR_WHATSAPP_SAUDI_DISPLAY")
        val creatorLinkedinUserAgent =
            creatorProperties.creatorValue("linkedinFetchUserAgent", "CREATOR_LINKEDIN_FETCH_USER_AGENT")

        buildConfigField("String", "CREATOR_GITHUB_USERNAME", creatorGithubUsername.asBuildConfigString())
        buildConfigField(
            "String",
            "CREATOR_GITHUB_PROFILE",
            "https://github.com/$creatorGithubUsername".asBuildConfigString(),
        )
        buildConfigField(
            "String",
            "CREATOR_GITHUB_API",
            "https://api.github.com/users/$creatorGithubUsername".asBuildConfigString(),
        )
        buildConfigField("String", "CREATOR_LINKEDIN_PROFILE", creatorLinkedinProfile.asBuildConfigString())
        buildConfigField("String", "CREATOR_LINKEDIN_HANDLE", creatorLinkedinHandle.asBuildConfigString())
        buildConfigField(
            "String",
            "CREATOR_LINKEDIN_AVATAR_URL",
            "https://unavatar.io/linkedin/$creatorLinkedinHandle".asBuildConfigString(),
        )
        buildConfigField("String", "CREATOR_LINKEDIN_FETCH_USER_AGENT", creatorLinkedinUserAgent.asBuildConfigString())
        buildConfigField(
            "String",
            "CREATOR_X_PROFILE",
            "https://x.com/$creatorXHandle".asBuildConfigString(),
        )
        buildConfigField("String", "CREATOR_X_HANDLE", creatorXHandle.asBuildConfigString())
        buildConfigField("String", "CREATOR_EMAIL", creatorEmail.asBuildConfigString())
        buildConfigField("String", "CREATOR_EMAIL_URL", "mailto:$creatorEmail".asBuildConfigString())
        buildConfigField("String", "CREATOR_WHATSAPP_INDIA_E164", creatorWhatsappIndiaE164.asBuildConfigString())
        buildConfigField("String", "CREATOR_WHATSAPP_SAUDI_E164", creatorWhatsappSaudiE164.asBuildConfigString())
        buildConfigField("String", "CREATOR_WHATSAPP_INDIA_DISPLAY", creatorWhatsappIndiaDisplay.asBuildConfigString())
        buildConfigField("String", "CREATOR_WHATSAPP_SAUDI_DISPLAY", creatorWhatsappSaudiDisplay.asBuildConfigString())
        buildConfigField(
            "String",
            "CREATOR_WHATSAPP_INDIA_URL",
            "https://wa.me/$creatorWhatsappIndiaE164".asBuildConfigString(),
        )
        buildConfigField(
            "String",
            "CREATOR_WHATSAPP_SAUDI_URL",
            "https://wa.me/$creatorWhatsappSaudiE164".asBuildConfigString(),
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
            if (!keystorePath.isNullOrBlank()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            isPseudoLocalesEnabled = false
            isCrunchPngs = true

            val keystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
            if (!keystorePath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }

            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*.kotlin_module",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        lintConfig = file("lint.xml")
        abortOnError = true
        checkAllWarnings = true
        checkDependencies = true
        htmlReport = true
        xmlReport = true
        textReport = true
        textOutput = file("${layout.buildDirectory.get()}/reports/lint-results-debug.txt")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.animation)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3) {
        because("Material 3 Expressive (sheets, LargeFlexibleTopAppBar, expressive shapes)")
    }
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.haze)
    implementation(libs.haze.materials)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.litert)
    implementation(libs.mediapipe.tasks.vision)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register<Exec>("downloadPretextVisionAssets") {
    group = "pretext"
    description = "One-time setup: download NCNN libs and vision models (run manually on fresh clone)."
    workingDir = rootProject.projectDir
    commandLine("bash", "scripts/download_pretext_vision_assets.sh")
}

tasks.register("assertNcnnLibsPresent") {
    group = "pretext"
    description = "Fail fast when NCNN static libs are missing (required for Object mode)."
    doLast {
        val lib = file("src/main/cpp/third_party/ncnn/arm64-v8a/lib/libncnn.a")
        check(lib.isFile && lib.length() > 0L) {
            "NCNN static lib missing at ${lib.path}. Run: ./gradlew downloadPretextVisionAssets"
        }
    }
}

tasks.matching { it.name.startsWith("configureCMake") }.configureEach {
    dependsOn("assertNcnnLibsPresent")
}

listOf("installDebug", "installRelease").forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        finalizedBy("resetLauncherAliases")
    }
}

tasks.register<Exec>("resetLauncherAliases") {
    group = "install"
    description = "Reset activity-alias enabled state so IDE launch works after icon switches"
    workingDir = rootProject.projectDir
    commandLine("bash", "scripts/reset_launcher_aliases.sh")
    isIgnoreExitValue = true
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
                "-opt-in=androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
                "-opt-in=androidx.compose.animation.ExperimentalSharedTransitionApi",
                "-opt-in=dev.chrisbanes.haze.ExperimentalHazeApi",
                "-opt-in=dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi",
            ),
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:deprecation",
            "-Xlint:unchecked",
        ),
    )
}
