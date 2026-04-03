import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven(url = "https://repo.runelite.net/") // RuneLite
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
        apiVersion.set(KotlinVersion.KOTLIN_2_1)

        jvmTarget.set(JvmTarget.JVM_21)

        //suppressWarnings.set(true)
    }
}
