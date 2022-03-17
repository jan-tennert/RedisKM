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
description = "A Kotlin Multiplatform Redis Client"

repositories {
    mavenCentral()
}

nexusStaging {
    stagingProfileId = Publishing.PROFILE_ID
    stagingRepositoryId.set(Publishing.REPOSITORY_ID)
    username = Publishing.SONATYPE_USERNAME
    password = Publishing.SONATYPE_PASSWORD
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
}

signing {
    val signingKey = providers
        .environmentVariable("GPG_SIGNING_KEY")
        .forUseAtConfigurationTime()
    val signingPassphrase = providers
        .environmentVariable("GPG_SIGNING_PASSPHRASE")
        .forUseAtConfigurationTime()

    if (signingKey.isPresent && signingPassphrase.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
        val extension = extensions
            .getByName("publishing") as PublishingExtension
        sign(extension.publications)
    }
}
publishing {
    repositories {
        maven {
            name = "Oss"
            setUrl {
                "https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/${Publishing.REPOSITORY_ID}"
            }
            credentials {
                username = Publishing.SONATYPE_USERNAME
                password = Publishing.SONATYPE_PASSWORD
            }
        }
        maven {
            name = "Snapshot"
            setUrl { "https://s01.oss.sonatype.org/content/repositories/snapshots/" }
            credentials {
                username = Publishing.SONATYPE_USERNAME
                password = Publishing.SONATYPE_PASSWORD
            }
        }
    }
//val dokkaOutputDir = "H:/Programming/Other/DiscordKMDocs"
    val dokkaOutputDir = "$buildDir/dokka/${name}"

    tasks.dokkaHtml {
        outputDirectory.set(file(dokkaOutputDir))
    }

    val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
        delete(dokkaOutputDir)
    }

    val javadocJar = tasks.register<Jar>("javadocJar") {
        dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaOutputDir)
    }

    publications {
        withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set(this.name)
                description.set("A Kotlin Multiplatform Redis Client")
                url.set("https://github.com/jan-tennert/RedisKM")
                licenses {
                    license {
                        name.set("GPL-3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/jan-tennert/RedisKM/issues")
                }
                scm {
                    connection.set("https://github.com/jan-tennert/RedisKM.git")
                    url.set("https://github.com/jan-tennert/RedisKM")
                }
                developers {
                    developer {
                        name.set("TheRealJanGER")
                        email.set("jan.m.tennert@gmail.com")
                    }
                }
            }
        }
    }
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
