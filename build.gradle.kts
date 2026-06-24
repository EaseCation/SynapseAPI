import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.gradle.api.publish.maven.MavenPublication

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.2.2"
}

group = "org.itxtech.synapse"
version = "1.0.0"
description = "Synapse API"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // API 依赖（会被打包到 shadow jar）
    api("org.msgpack:jackson-dataformat-msgpack:0.9.10") {
        exclude(group = "com.fasterxml.jackson.core", "jackson-databind")
    }

    // 从 JitPack 引用已发布的 Nukkit
    compileOnly("com.github.EaseCation:Nukkit:master-SNAPSHOT")

    // 从 JitPack 引用 authlib-stub（编译时存根，运行时由 AuthLibPackage 插件提供真实实现）
    compileOnly("com.github.EaseCation:authlib-stub:master-SNAPSHOT")

    // 注解处理和编译时依赖
    compileOnly("org.projectlombok:lombok:1.18.38")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // 测试依赖
    testImplementation("com.github.EaseCation:Nukkit:master-SNAPSHOT")
}

// 配置编译选项
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// 配置 Javadoc（忽略错误，避免构建失败）
tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        addStringOption("encoding", "UTF-8")
        addStringOption("charSet", "UTF-8")
    }
    isFailOnError = false
}

// 普通 jar 使用 thin classifier
tasks.jar {
    archiveClassifier.set("thin")
}

// 配置 shadowJar 任务
tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    archiveClassifier.set("")  // shadow jar 作为主 artifact

    // Log4j2 插件缓存转换
    transform(Log4j2PluginsCacheFileTransformer::class.java)

    // 合并 service 文件
    mergeServiceFiles()

    // 排除不需要的文件
    exclude("**/module-info.class")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
}

// 创建源码 JAR
tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

// 创建 Javadoc JAR
tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
    dependsOn(tasks.javadoc)
}

// Maven 发布配置
publishing {
    publications.create<MavenPublication>("maven") {
        // 发布 shadow jar 作为主 artifact（包含 msgpack 依赖）
        artifact(tasks.shadowJar.get()) {
            classifier = ""
        }

        // 添加源码和文档 artifact
        artifact(tasks["sourcesJar"])
        artifact(tasks["javadocJar"])

        // 设置 Maven 坐标
        groupId = project.group.toString()
        artifactId = project.name
        version = project.version.toString()

        // 配置 POM 元数据
        pom {
            name = "SynapseAPI"
            description = "Synapse protocol API for Nukkit server communication - EaseCation implementation"
            url = "https://github.com/EaseCation/SynapseAPI"

            licenses {
                license {
                    name = "GNU General Public License v3.0"
                    url = "https://www.gnu.org/licenses/gpl-3.0.html"
                }
            }

            developers {
                developer {
                    id = "easecation"
                    name = "EaseCation Team"
                    url = "https://github.com/EaseCation"
                }
            }

            scm {
                connection = "scm:git:git://github.com/EaseCation/SynapseAPI.git"
                developerConnection = "scm:git:ssh://github.com/EaseCation/SynapseAPI.git"
                url = "https://github.com/EaseCation/SynapseAPI"
            }

            // 自定义依赖列表：只包含 compileOnly 依赖作为 provided scope
            withXml {
                val dependenciesNode = asNode().appendNode("dependencies")

                // 添加 Nukkit 作为 provided 依赖
                val nukkitDep = dependenciesNode.appendNode("dependency")
                nukkitDep.appendNode("groupId", "com.github.EaseCation")
                nukkitDep.appendNode("artifactId", "Nukkit")
                nukkitDep.appendNode("version", "master-SNAPSHOT")
                nukkitDep.appendNode("scope", "provided")

                // 添加其他 compileOnly 依赖
                configurations.compileOnly.get().allDependencies.forEach { dep ->
                    if (dep.group != null && dep.name != "unspecified" &&
                        dep.group != "com.github.EaseCation") {
                        val dependencyNode = dependenciesNode.appendNode("dependency")
                        dependencyNode.appendNode("groupId", dep.group)
                        dependencyNode.appendNode("artifactId", dep.name)
                        dependencyNode.appendNode("version", dep.version)
                        dependencyNode.appendNode("scope", "provided")
                    }
                }
            }
        }
    }
}
