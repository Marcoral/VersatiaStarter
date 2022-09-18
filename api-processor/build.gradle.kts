import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kspVersion: String by project
val kotlinpoetVersion: String by project
val spigotVersion: String by project

dependencies {
    implementation(project(":api"))
    implementation("com.squareup:kotlinpoet:$kotlinpoetVersion")
    implementation("com.squareup:kotlinpoet-ksp:$kotlinpoetVersion")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    implementation("org.spigotmc:spigot-api:$spigotVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}
