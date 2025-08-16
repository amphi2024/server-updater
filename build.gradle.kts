plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.amphi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib")) // 필수
    implementation("org.json:json:20250517")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

// ShadowJar로 fat jar 만들기
tasks.shadowJar {
    archiveFileName.set("updater.jar")
    manifest {
        attributes(
            "Main-Class" to "com.amphi.MainKt"
        )
    }
}
