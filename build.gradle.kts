plugins {
    id("ecbuild.java-conventions")
    id("ecbuild.copy-conventions")
}

extra.set("copyTo", listOf("{server}/plugins", "{login}/plugins", "{server1}/plugins"))

dependencies {
    api(libs.org.msgpack.msgpack.core) {
        exclude("com.fasterxml.jackson.core", module = "jackson-databind")
    }
    compileOnly(project(":nukkit"))
    val authLibPkg = findProject(":AuthLibPackage")
    if (authLibPkg == null) {
        compileOnly(files(File(rootProject.projectDir, "lib/AuthLibPackage.jar")))
    } else {
        compileOnly(project(":AuthLibPackage"))
    }
    testImplementation(project(":nukkit"))
}

group = "org.itxtech.synapse"
description = "Synapse API"
