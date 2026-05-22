import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val generateBuildConfig = tasks.register("generateBuildConfig") {
    val localPropertiesFile = project.rootProject.file("local.properties")
    val outputDir = layout.buildDirectory.dir("generated/source/buildconfig/main")
    
    inputs.file(localPropertiesFile).optional()
    outputs.dir(outputDir)
    
    doLast {
        val properties = Properties()
        if (localPropertiesFile.exists()) {
            FileInputStream(localPropertiesFile).use { fis ->
                properties.load(fis)
            }
        }
        val ip = properties.getProperty("vacuum.ip") ?: "192.168.1.150"
        val token = properties.getProperty("vacuum.token") ?: "4a526f6b5f546f6b656e5f5f5f5f5f5f"
        
        val outputFile = outputDir.get().file("com/vahitkeskin/loopsweep/BuildConfig.kt").asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package com.vahitkeskin.loopsweep

            object BuildConfig {
                const val VACUUM_IP: String = "$ip"
                const val VACUUM_TOKEN: String = "$token"
            }
            """.trimIndent()
        )
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    androidLibrary {
       namespace = "com.vahitkeskin.loopsweep.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.timber)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain {
            kotlin.srcDir(generateBuildConfig)
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.ktor.network)
                implementation(libs.ktor.client.core)
                implementation(libs.androidx.datastore)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.jetbrains.navigation.compose)
            }
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}