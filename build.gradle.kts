plugins {
    id("ecbuild.java-conventions")
    id("ecbuild.copy-conventions")
}

extra.set("copyTo", listOf("{server}/plugins", "{login}/plugins", "{server1}/plugins"))

dependencies {
    api(libs.org.msgpack.msgpack.core)
    compileOnly(libs.netty.all)
    compileOnly(project(":Nukkit"))
    val authLibPkg = findProject(":AuthLibPackage")
    if (authLibPkg == null) {
        compileOnly(files(File(rootProject.projectDir, "lib/AuthLibPackage.jar")))
    } else {
        compileOnly(project(":AuthLibPackage"))
    }
}

group = "org.itxtech.synapse"
description = "Synapse API"
