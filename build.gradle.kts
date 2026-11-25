plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.jspecify:jspecify:1.0.0")

    implementation("org.xerial:sqlite-jdbc:3.50.3.0")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.15.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.15.2")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

val testServers = mapOf(
    "26.1.1" to 25,
    "1.21.11" to 21,
    "1.20.4" to 17,
)

testServers.entries.forEachIndexed { index, (minecraftVersion, javaVersion) ->
    val major = minecraftVersion.split(".").take(2).joinToString("_")
    val port = 25565 + index
    tasks.register<xyz.jpenilla.runpaper.task.RunServer>("runServer$major") {
        group = "run paper"
        description = "Runs a Paper $minecraftVersion test server on Java $javaVersion with this plugin (port $port)."
        args("--port", port.toString())
        minecraftVersion(minecraftVersion)
        pluginJars(tasks.jar.flatMap { it.archiveFile })
        runDirectory = layout.projectDirectory.dir("run/$major")
        javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(javaVersion) }
        jvmArgs("-Xms2G", "-Xmx2G")
    }
}

tasks {
    runServer {
        minecraftVersion("26.1.1")
        javaLauncher = project.javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    test {
        useJUnitPlatform()
    }
}