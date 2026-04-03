plugins {
    id("near-reality-project-kotlin")
}

group = "com.near_reality.plugins.factionwars"
version = "0.1.0"

dependencies {
    implementation(projects.core)
    implementation(projects.api)
    implementation(projects.scripts.npc.actions)
    implementation(projects.scripts.npc)
}
