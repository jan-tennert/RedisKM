plugins {
    kotlin("multiplatform") version Versions.KOTLIN
    kotlin("plugin.serialization") version Versions.KOTLIN
}

group = "io.github.jan.rediskm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }
    js(IR) {
        browser()
        nodejs()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("com.soywiz.korlibs.korio:korio:${Versions.KORLIBS}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.SERIALIZATION}")
            }
        }
        val jvmMain by getting
        val jsMain by getting
    }
}
