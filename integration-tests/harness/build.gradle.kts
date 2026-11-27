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
    // Same as SimpleFactions itself: Java 16 bytecode runs on every server from 1.17.1
    // (on Java 16) up to 26.x.
    toolchain.languageVersion = JavaLanguageVersion.of(16)
}

tasks.jar {
    archiveFileName.set("SimpleFactionsTestHarness.jar")
}
