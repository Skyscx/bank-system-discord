plugins {
    java
    `java-library`
    kotlin("jvm") version "1.9.0"
}

allprojects {
    group = "me.topilov"
    project.version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven {
            url = uri("https://papermc.io/repo/repository/maven-public/")
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        withSourcesJar()
    }

    tasks {
        withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
}