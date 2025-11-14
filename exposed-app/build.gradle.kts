// Module: exposed-app

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

group = "me.bossm0n5t3r"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Exposed ORM (from version catalog)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)

    // Connection pool & Driver (reuse versions from version catalog)
    implementation(libs.hikari)
    implementation(libs.postgresql)

    // Simple logging
    runtimeOnly(libs.slf4j.simple)

    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(
        libs.versions.jdk
            .get()
            .toInt(),
    )
}

ktlint {
    version.set(
        libs.versions.pinterest.ktlint
            .get(),
    )
}
