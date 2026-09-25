plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
}

import com.vanniktech.maven.publish.SonatypeHost

val sdkGroupId = "io.github.naveenmamgain14"
val sdkArtifactId = "storyly"
val sdkVersion = "0.1.0"

android {
    namespace = "com.storyly.sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // Default methods on public interfaces so Java consumers can implement listeners cleanly
        freeCompilerArgs += "-Xjvm-default=all"
    }

    buildFeatures {
        compose = true
    }
}

// Every public declaration must state its visibility and return type.
// This is what keeps the published API surface deliberate rather than accidental.
kotlin {
    explicitApi()
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)

    // Compose: kept to foundation only. Deliberately NOT material3 — consumers
    // should not be forced onto a design system by a story widget.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.datasource.okhttp)

    testImplementation(libs.junit)
}

mavenPublishing {
    coordinates(sdkGroupId, sdkArtifactId, sdkVersion)

    pom {
        name.set("Storyly")
        description.set("Instagram-style stories for Android, with image, GIF and video support.")
        inceptionYear.set("2026")
        url.set("https://github.com/naveenmamgain14/storyly")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("naveenmamgain14")
                name.set("naveenmamgain14")
                url.set("https://github.com/naveenmamgain14")
            }
        }
        scm {
            url.set("https://github.com/naveenmamgain14/storyly")
            connection.set("scm:git:git://github.com/naveenmamgain14/storyly.git")
            developerConnection.set("scm:git:ssh://git@github.com/naveenmamgain14/storyly.git")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)

    // Signing is required by Central but must not block a local publish, so it
    // switches on only once a key is actually configured.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }
}
