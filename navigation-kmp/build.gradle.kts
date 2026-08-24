import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    id("maven-publish")
}

val versionProvider = providers.exec {
    commandLine("git", "describe", "--tags", "--abbrev=0")
    isIgnoreExitValue = true
}.standardOutput.asText.map { output ->
    val version = output.trim()
    if (version.isNotEmpty() && version.startsWith("v")) {
        version.substring(1)
    } else version.ifEmpty {
        "1.0.0"
    }
}.orElse("1.0.0")

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release")
    }


    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Navigation"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.jetbrains.navigation.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            // Napoli Base
            implementation(libs.napoli.base)
            implementation(libs.napoli.logger)
            

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "cl.baldomeronapoli.navigation"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// Fix para dependencias de tareas de sourceJar
tasks.configureEach {
    if (name == "sourceReleaseJar") {
        dependsOn(
            "generateResourceAccessorsForAndroidMain",
            "generateActualResourceCollectorsForAndroidMain"
        )
    }
}

publishing {
    repositories {
        mavenLocal()

        maven {
            name = "Local"
            url = uri(layout.buildDirectory.dir("repo"))
        }

        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/elNapoli/kmm-navigation")
            credentials {
                username = project.findProperty("gpr.user") as String?
                    ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.token") as String?
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

afterEvaluate {
    publishing {
        publications.withType<MavenPublication> {
            groupId = "cl.baldomeronapoli"
            version = versionProvider.get()
        }
    }
}