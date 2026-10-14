rootProject.name = "SynapseAPI"

pluginManagement {
    repositories {
        // JitPack出口访问Maven Central可能被限流，优先使用其Google只读镜像。
        maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2") }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2") }
        mavenCentral {
            content { excludeGroupByRegex("com\\.github\\..*") }
        }
        maven { url = uri("https://jitpack.io") }
    }
}
