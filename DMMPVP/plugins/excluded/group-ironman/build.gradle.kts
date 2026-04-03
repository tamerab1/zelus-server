plugins {
    id("near-reality-project-kotlin")
    kotlin("plugin.serialization") version "1.8.22"
}

group = "com.near_reality.plugins"
version = "0.1.0"

dependencies {
    implementation(libs.apache.commons.lang3)
    implementation(projects.core)

    implementation(projects.scripts.npc.actions)
    implementation(projects.scripts.npc.spawns)
    implementation(projects.scripts.item.actions)
    implementation(projects.scripts.player.actions)
    implementation(projects.scripts.`object`.actions)

    implementation(projects.scripts.interfaces)
    implementation(projects.plugins.larranskey)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
}

tasks.test {
    useJUnitPlatform()
    workingDir = file("../../../")
}
