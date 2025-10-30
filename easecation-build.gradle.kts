plugins {
    id("ecbuild.java-conventions")
    id("ecbuild.copy-conventions")
}

group = "org.itxtech.synapse"
version = "1.0.0"
description = "Synapse API"

extra.set("copyTo", listOf("{server}", "{proxy}", "{login-proxy}"))

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // API 依赖（会被打包到 shadow jar）
    api("org.msgpack:jackson-dataformat-msgpack:0.9.10") {
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-databind")
    }

    // 依赖当前项目中的 Nukkit
    compileOnly(project(":nukkit"))

    // 依赖当前项目中的 authlib-stub
    compileOnly(project(":authlib-stub"))
}

// 配置 shadowJar 任务（继承自 ecbuild.java-conventions）
tasks.shadowJar {
    // 排除不需要的文件（合并 conventions 的默认配置）
    exclude("**/module-info.class")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
}
