plugins {
    kotlin("jvm") version "1.9.22"
    id("antlr")
}

group = "com.skillw.asaka"
version = "1.1-SNAPSHOT"


repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("org.ow2.asm:asm:9.6")
    implementation("org.ow2.asm:asm-util:9.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.antlr:antlr4:4.13.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}