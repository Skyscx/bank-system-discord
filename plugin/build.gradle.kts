plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")
}

tasks {
    jar { enabled = false }
    shadowJar { archiveFileName.set("destruction.jar") }
    build { dependsOn(shadowJar) }
}