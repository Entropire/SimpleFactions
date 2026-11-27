plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "SimpleFactions"
include(":integration-tests:harness", ":integration-tests:runner")