# SynapseAPI

[![](https://jitpack.io/v/EaseCation/SynapseAPI.svg)](https://jitpack.io/#EaseCation/SynapseAPI)

Synapse protocol API for Nukkit server communication - EaseCation implementation.

## 关于

SynapseAPI 是 EaseCation 分布式架构中的关键通信协议实现，负责 Nukkit 游戏服务器与 Nemisys 代理服务器之间的通信。它提供了一套完整的跨服务器通信机制，支持玩家转移、数据同步、消息传递等核心功能。

## 功能特性

- **玩家转移**: 玩家在不同游戏服务器间的无缝切换
- **数据同步**: 跨服务器的游戏数据同步
- **消息传递**: 完整的消息定义和序列化机制
- **连接管理**: 连接池、重连、心跳等连接管理
- **负载均衡**: 服务器负载监控和智能分配

## 依赖引入

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.EaseCation:SynapseAPI:1.0.0")
    // 或使用最新版本
    implementation("com.github.EaseCation:SynapseAPI:master-SNAPSHOT")
}
```

### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.EaseCation:SynapseAPI:1.0.0'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.EaseCation</groupId>
        <artifactId>SynapseAPI</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## 构建说明

本项目使用 Gradle 9.1.0 和 Java 21 构建。

```bash
# 构建项目
./gradlew build

# 构建 shadow jar（包含依赖）
./gradlew shadowJar

# 发布到本地 Maven
./gradlew publishToMavenLocal
```

## 依赖说明

### 编译时依赖

- **Nukkit**: 作为 provided 依赖，不会打包到最终 JAR 中
- **MessagePack**: 用于消息序列化，会打包到 shadow JAR 中
- **authlib-stub**: 网易认证库的编译时存根，不会打包到最终 JAR 中
- **Lombok**: 编译时注解处理器

### 运行时依赖

SynapseAPI 支持国际版和网易版客户端：

- **国际版客户端**: 无需额外依赖，开箱即用
- **网易版客户端**: 需要部署 **AuthLibPackage 插件**（私有，联系 EaseCation 获取）

**技术说明**: SynapseAPI 使用 [authlib-stub](https://github.com/EaseCation/authlib-stub) 作为编译时依赖。运行时，如果部署了 AuthLibPackage 插件，其提供的真实 authlib 实现会通过类加载器优先级机制覆盖 stub 实现，从而支持网易版客户端的认证功能

## 许可证

本项目基于 [GNU General Public License v3.0](LICENSE) 开源。

## 相关项目

- [Nukkit](https://github.com/EaseCation/Nukkit) - Minecraft 基岩版服务器核心
- [Nemisys](https://github.com/EaseCation/Nemisys) - 网络代理服务器
- [CodeFunCore](https://github.com/EaseCation/CodeFunCore) - 主服务器核心插件
