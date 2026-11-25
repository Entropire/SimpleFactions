plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("org.jspecify:jspecify:1.0.0")

    implementation("org.xerial:sqlite-jdbc:3.50.3.0")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.15.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.15.2")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(16)
}

val testServers = mapOf(
    "26.1.1" to 25,
    "1.21.11" to 21,
    "1.20.4" to 17,
    "1.19.4" to 17,
    "1.18.2" to 17,
    "1.17.1" to 16,
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

val bundledSqliteDrivers = mapOf(
    "1.18.2" to "3.36.0.3",
    "1.17.1" to "3.34.0",
)

bundledSqliteDrivers.forEach { (minecraftVersion, driverVersion) ->
    val major = minecraftVersion.split(".").take(2).joinToString("_")
    val driver = configurations.create("sqliteDriver$major") {
        isCanBeConsumed = false
        isTransitive = false
    }
    dependencies.add(driver.name, "org.xerial:sqlite-jdbc:$driverVersion")

    val testTask = tasks.register<Test>("testSqlite$major") {
        group = "verification"
        description = "Runs the tests against sqlite-jdbc $driverVersion, the driver Paper $minecraftVersion ships."
        useJUnitPlatform()
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = driver + sourceSets.test.get().runtimeClasspath.filter { !it.name.startsWith("sqlite-jdbc-") }
        systemProperty("simplefactions.expectedSqliteVersion", driverVersion.split(".").take(3).joinToString("."))
    }
    tasks.check { dependsOn(testTask) }
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