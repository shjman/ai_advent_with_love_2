import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val claudeApiKey: String = localProperties.getProperty("CLAUDE_API_KEY", "")

val moduleDomainModels = ":domain-models"

android {
    namespace = "com.yahorshymanchyk.ai_advent_with_love_2.feature.claude"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        buildConfigField("String", "CLAUDE_API_KEY", "\"$claudeApiKey\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        buildConfig = true
    }

    lint {
        lintConfig = rootProject.file("lint.xml")
        xmlReport = true
        abortOnError = true
        warningsAsErrors = false
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/NOTICE",
            "META-INF/NOTICE.txt"
        )
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    api(project(moduleDomainModels))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.anthropic.java)
    implementation(libs.timber)
}
