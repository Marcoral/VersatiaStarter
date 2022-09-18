pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("com.google.devtools.ksp") version kspVersion
    }

    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "VersatiaStarter"
include(":api")
include(":api-processor")
