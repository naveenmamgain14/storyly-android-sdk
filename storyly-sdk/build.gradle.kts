plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
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

    testImplementation(libs.junit)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = sdkGroupId
            artifactId = sdkArtifactId
            version = sdkVersion

            afterEvaluate { from(components["release"]) }

            pom {
                name.set("Storyly")
                description.set("Instagram-style stories for Android, with image, GIF and video support.")
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
                    }
                }
                scm {
                    url.set("https://github.com/naveenmamgain14/storyly")
                    connection.set("scm:git:git://github.com/naveenmamgain14/storyly.git")
                }
            }
        }
    }
}
