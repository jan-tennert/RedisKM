plugins {
    kotlin("multiplatform") version Versions.KOTLIN
    kotlin("plugin.serialization") version Versions.KOTLIN
    id("maven-publish")
    signing
    id("org.jetbrains.dokka") version Versions.DOKKA
    id("io.codearte.nexus-staging") version Versions.NEXUS_STAGING
}

group = "io.github.jan-tennert.rediskm"
version = Versions.REDISKM

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
    mingwX64()
    linuxX64()
    macosX64()
    macosArm64()
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
