@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
//    id(libs.plugins.kotlin.multiplatform.get().pluginId)
//    id(libs.plugins.android.library.get().pluginId)

    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)

    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @Suppress("OPT_IN_USAGE")
    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xexpect-actual-classes", // remove warnings for expect classes
            "-Xskip-prerelease-check",
            "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
            "-opt-in=org.jetbrains.compose.resources.InternalResourceApi",
        )
    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmTarget.get()))
    }
    applyDefaultHierarchyTemplate()
    androidTarget {
        publishLibraryVariants("release")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        // 包含 Objective-C 文件
        it.compilerOptions {

        }
    }


    sourceSets {
        commonMain.get().apply {
            resources.srcDir("src/commonMain/composeResources")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.components.resources)

            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(project.dependencies.platform(libs.coroutines.bom))
            implementation(project.dependencies.platform(libs.coil.bom))
//            implementation(libs.kmm.navigation.compose)
            implementation(libs.coil.core)
            api(libs.coil.compose)

            implementation("com.squareup.okio:okio:3.9.0")
        }

        androidMain.dependencies {
            implementation(libs.accompanist.permissions)
            implementation(libs.androidx.navigation.compose)
//            implementation(libs.androidx.documentfile)
        }

        iosMain.get().apply {
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "compose_filepicker.filepicker.generated.resources"
    generateResClass = always
}

android {
    namespace = "com.github.jing332.filepicker"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    lint {
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
    dependencies {
        implementation(compose.uiTooling)
    }
}