plugins {
    java
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Compiled against the OLDEST supported API (Spigot 1.17.1), so the harness itself
    // can't accidentally use a method that older servers or Spigot/CraftBukkit don't have.
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(16)
}

tasks.jar {
    archiveFileName.set("SimpleFactionsTestHarness.jar")
}
