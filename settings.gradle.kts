rootProject.name = "SynapseAPI"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // 尝试从父项目加载版本目录，如果不存在则从本地加载
            val parentLibsFile = file("../gradle/libs.versions.toml")
            if (parentLibsFile.exists()) {
                from(files(parentLibsFile))
            } else {
                // 独立构建时的版本定义
                version("shadow", "9.2.2")
                plugin("shadow", "com.gradleup.shadow").versionRef("shadow")
            }
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
