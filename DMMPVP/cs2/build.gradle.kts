plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

group = "org.runestar.cs2"
version = "0.1.0-SNAPSHOT"

description = "cs2"

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}