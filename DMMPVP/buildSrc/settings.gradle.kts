dependencyResolutionManagement {
    versionCatalogs {
        create("libs") { from(files("../gradle/libs.versions.toml")) }
    }
    repositories {
        flatDir {
            dirs("../lib")
        }
    }
}

rootProject.name = "buildSrc"
