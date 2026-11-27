plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

evaluationDependsOn(":integration-tests:harness")

// The normal test task stays off, so `gradlew build` never starts 40+ servers.
tasks.test {
    enabled = false
}

val integrationTest by tasks.registering(Test::class) {
    description = "Starts the servers in servers.csv (-Psize=smoke|default|full) in Docker and runs the in-server tests on each."
    group = "verification"
    mustRunAfter(":check")

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()

    val pluginJar = rootProject.tasks.named<Jar>("jar").flatMap { it.archiveFile }
    val harnessJar = project(":integration-tests:harness").tasks.named<Jar>("jar").flatMap { it.archiveFile }
    val servers = providers.gradleProperty("servers").orElse("")
    val runsVerify = gradle.startParameter.taskNames.any { it == "verify" || it.endsWith(":verify") }
    val size = providers.gradleProperty("size").orElse(servers.map { filter ->
        when {
            runsVerify -> "smoke"
            filter.isNotBlank() -> "full"
            else -> "default"
        }
    })
    val parallel = providers.gradleProperty("parallel").orElse("2")
    val timeoutMinutes = providers.gradleProperty("startupTimeoutMinutes").orElse("20")
    val cacheDir = layout.projectDirectory.dir("../.cache").asFile.canonicalPath
    val serversFile = layout.projectDirectory.file("servers.csv").asFile.absolutePath
    val logsDir = layout.buildDirectory.dir("server-logs").get().asFile.absolutePath

    inputs.file(pluginJar)
    inputs.file(harnessJar)
    outputs.upToDateWhen { false }

    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "-Dsf.pluginJar=${pluginJar.get().asFile.absolutePath}",
            "-Dsf.harnessJar=${harnessJar.get().asFile.absolutePath}",
            "-Dsf.servers=${servers.get()}",
            "-Dsf.size=${size.get()}",
            "-Dsf.parallel=${parallel.get()}",
            "-Dsf.startupTimeoutMinutes=${timeoutMinutes.get()}",
            "-Dsf.cacheDir=$cacheDir",
            "-Dsf.serversFile=$serversFile",
            "-Dsf.logsDir=$logsDir",
        )
    })

    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
