plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")
    implementation("net.dv8tion:JDA:5.0.1")
    compileOnly("com.discordsrv:discordsrv:1.27.0") }

tasks {
    jar { enabled = false }
    shadowJar { archiveFileName.set("plugin.jar") }
    build { dependsOn(shadowJar) }
}