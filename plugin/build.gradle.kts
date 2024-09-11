plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    implementation("net.dv8tion:JDA:5.0.1")
    compileOnly("com.discordsrv:discordsrv:1.27.0")
    implementation("net.wesjd:anvilgui:1.10.2-SNAPSHOT")
}

tasks {
    jar { enabled = false }
    shadowJar { archiveFileName.set("plugin.jar") }
    build { dependsOn(shadowJar) }
}