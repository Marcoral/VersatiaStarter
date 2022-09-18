import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val spigotVersion: String by project
val kotlinVersion: String by project

plugins {
    `kotlin-dsl`
    kotlin("jvm")
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

allprojects {
    group = "com.github.marcoral.versatia"
    version = "1.0.0"

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")

    publishing {
        publications {
            create<MavenPublication>("myLibrary") {
                from(components["kotlin"])
            }
        }

        repositories {
            mavenLocal()
        }
    }

    dependencies {
        compileOnly("org.spigotmc:spigot-api:$spigotVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    }
}

dependencies {
    api(project(":api"))
    api(project(":api-processor"))
    api("org.spigotmc:spigot-api:$spigotVersion")
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
