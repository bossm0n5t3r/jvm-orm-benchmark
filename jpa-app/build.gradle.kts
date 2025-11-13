// Module: jpa-app

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
    // JPA / Hibernate
    implementation(libs.hibernate.core)
    implementation(libs.jakarta.persistence.api)

    // Connection pool & DB (PostgreSQL)
    implementation(libs.hikari)
    runtimeOnly(libs.postgresql)

    // Logging (Hibernate uses SLF4J)
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
