import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    id(libs.plugins.jetbrains.compose.get().pluginId)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmTarget.get()))
    }
    applyDefaultHierarchyTemplate()
    androidTarget {
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            binaryOption("bundleId", "com.github.jing332.compose_filepicker")
            if (System.getenv("XCODE_VERSION_MAJOR") == "1500") {
                linkerOpts += "-ld_classic"
            }
        }

    }
    targets.withType<KotlinNativeTarget> {
        binaries.filterIsInstance<Framework>().forEach {
            it.linkerOpts += "-ObjC"
            // 内存分配器
            it.freeCompilerArgs += "-Xallocator=mimalloc"
        }
    }

    task("testClasses")
    sourceSets {
        commonMain.get().apply {
            resources.srcDir("src/commonMain/composeResources")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.components.resources)

            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(project.dependencies.platform(libs.coroutines.bom))
            implementation(project.dependencies.platform(libs.coil.bom))
            implementation(project.dependencies.platform(libs.ktor.bom))

            implementation(libs.kotlinx.datetime)
//            implementation(libs.kmm.navigation.compose)

            implementation(projects.filePicker)

            // toast
            implementation("com.vickyleu.sonner:sonner:1.0.2")

            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha02")


            implementation(libs.ktor.http)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.logging)

            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.serialization.json)
            implementation(libs.ktor.client.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.ktor.client.websocket)
            implementation(libs.ktor.client.resource)

        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            implementation("androidx.appcompat:appcompat:1.7.0")
        }
    }
}

android {
    namespace = "com.github.jing332.compose_filepicker"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    defaultConfig {
        applicationId = "com.github.jing332.compose_filepicker"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    lint {
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }

    buildFeatures.compose = true

    dependencies {
        implementation(compose.uiTooling)
    }
}