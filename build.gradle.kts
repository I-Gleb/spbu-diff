plugins {
    kotlin("jvm") version "1.5.10"
    application
}

group = "ru.spbu.math-cs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}