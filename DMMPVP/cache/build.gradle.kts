import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolutePathString

plugins {
    id("near-reality-project-kotlin")
}

group = "com.near_reality"
version = "0.1.0"

dependencies {
    api(projects.util)

    implementation(libs.jackson.dataformat.toml)

    // START - for jire to change
    api(projects.coreModel)
    // END - for jire to change

    implementation(projects.util)
    implementation(libs.kryo)
    implementation(libs.google.guava)
    implementation(libs.apache.commons.io)
    implementation(libs.zip4j)
    implementation(libs.apache.commons.compress)
    implementation(libs.apache.commons.lang3)
    implementation(libs.jsoup)
    implementation(libs.tradukisto)
    implementation(libs.apache.ant)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.apache.commons.cli)

    implementation(projects.cs2)

    api(libs.toml4j)

    api(libs.runelite.api)
    api(libs.runelite.cache)
}

val cache225Name = "cache-225.zip"
val cache225URL = "https://archive.openrs2.org/caches/runescape/1943/disk.zip"

val keys225Name = "cache-225-keys.json"
val keys225URL = "https://archive.openrs2.org/caches/runescape/1943/keys.json"

tasks.register("download225Cache") {
    doLast {
        val dataDirectory = projectDir.toPath().resolve("data")

        val cache = dataDirectory.resolve(cache225Name)
        if (Files.notExists(cache)) {
            println("Cache (225) does not exist. Downloading...")
            Files.createDirectories(dataDirectory)
            URI.create(cache225URL)
                .toURL()
                .openStream()
                .use { inputStream ->
                    Files.copy(
                        inputStream,
                        cache,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            println("Download complete: ${cache.absolutePathString()}")
        } else {
            println("Cache (225) already exists at: ${cache.absolutePathString()}")
        }

        val keys = dataDirectory.resolve(keys225Name)
        if (Files.notExists(keys)) {
            println("Keys (225) does not exist. Downloading...")
            Files.createDirectories(dataDirectory)
            URI.create(keys225URL)
                .toURL()
                .openStream()
                .use { inputStream ->
                    Files.copy(
                        inputStream,
                        keys,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            println("Download complete: ${keys.absolutePathString()}")
        } else {
            println("Keys (225) already exists at: ${keys.absolutePathString()}")
        }
    }
}

tasks.named("build") {
    dependsOn("download225Cache")
}

tasks.register<JavaExec>("packCustomItems") {
    group = "near-reality"
    description = "Packs custom item models into the cache and updates ItemDefinitions"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("mgi.tools.parser.TypeParser")
    workingDir = File(rootDir, "cache")
    dependsOn("classes")
}
