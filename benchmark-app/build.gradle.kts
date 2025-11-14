import kotlinx.benchmark.gradle.JvmBenchmarkTarget

// Module: benchmark-app

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.all.open)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlinx.benchmark)
}

group = "me.bossm0n5t3r"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Will depend on jpa-app and exposed-app when benchmarks are implemented
    implementation(project(":jpa-app"))
    implementation(project(":exposed-app"))
    implementation(libs.kotlinx.benchmark.runtime)
    // Ensure runtime has JDBC driver & simple logger (not inherited transitively)
    runtimeOnly(libs.postgresql)
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

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

benchmark {
    targets {
        register("main") {
            this as JvmBenchmarkTarget
            jmhVersion = "1.37"
        }
    }
    configurations {
        named("main") {
            warmups = 10
            iterations = 10
            iterationTime = 3
            iterationTimeUnit = "ns"
            mode = "AverageTime"
            reportFormat = "csv"
            reportsDir = "$rootDir/benchmark-reports"
        }
    }
}
