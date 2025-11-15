import groovy.json.JsonSlurper
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
            reportFormat = "json"
            reportsDir = "$rootDir/benchmark-reports"
        }
    }
}

// ★ 루트 프로젝트 기준 경로
val benchmarkResultsDir = rootProject.layout.projectDirectory.dir("benchmark-reports/main")
val benchmarkDoc = rootProject.layout.projectDirectory.file("docs/benchmark.md")

val updateBenchmarkDoc by tasks.registering {
    outputs.file(benchmarkDoc)

    doLast {
        val resultsDir = benchmarkResultsDir.asFile
        if (!resultsDir.exists()) {
            logger.warn("Benchmark results dir not found: ${resultsDir.path}")
            return@doLast
        }

        // benchmark-reports/main/**/main.json 찾기
        val reports =
            fileTree(resultsDir) {
                include("**/main.json")
            }.files

        if (reports.isEmpty()) {
            logger.warn("No main.json benchmark reports found under ${resultsDir.path}")
            return@doLast
        }

        val reportFile = reports.maxBy { it.lastModified() }
        logger.lifecycle("Using benchmark report: ${reportFile.path}")

        @Suppress("UNCHECKED_CAST")
        val parsed = JsonSlurper().parse(reportFile) as List<Map<String, Any?>>

        // 1) 테이블 생성
        val header =
            buildString {
                appendLine("| Benchmark | batchSize | rows | Score (s/op) | Error | 95% CI |")
                appendLine("|-----------|-----------|------|--------------|-------|--------|")
            }

        val rows =
            parsed.joinToString("\n") { bm ->
                val fullName = bm["benchmark"] as? String ?: "unknown"
                val simpleName = fullName.substringAfterLast('.')

                val params = bm["params"] as? Map<*, *> ?: emptyMap<Any, Any>()
                val batchSize = params["batchSize"] ?: "-"
                val rowsParam = params["rows"] ?: "-"

                val primary = bm["primaryMetric"] as? Map<*, *>
                val score = primary?.get("score") ?: "-"
                val error = primary?.get("scoreError") ?: "-"

                val confidence = primary?.get("scoreConfidence") as? List<*>
                val ciLow = confidence?.getOrNull(0)
                val ciHigh = confidence?.getOrNull(1)
                val ci = if (ciLow != null && ciHigh != null) "$ciLow ~ $ciHigh" else "-"

                "| $simpleName | $batchSize | $rowsParam | $score | $error | $ci |"
            }

        val tableMarkdown = header + rows + "\n"

        // 2) 기존 md 에서 마커 사이 영역 정확히 잡기
        val docFile = benchmarkDoc.asFile
        if (!docFile.exists()) {
            throw GradleException("Benchmark doc not found: ${docFile.path}")
        }

        val original = docFile.readText()

        val startMarker = "<!-- benchmark-table:start -->"
        val endMarker = "<!-- benchmark-table:end -->"

        val startIndex = original.indexOf(startMarker)
        val endIndex = original.indexOf(endMarker)

        if (startIndex == -1 || endIndex == -1) {
            throw GradleException("benchmark.md 에 $startMarker / $endMarker 마커를 먼저 넣어주세요.")
        }

        // 마커 바로 뒤 위치 (내용 시작 위치)
        val contentStart = startIndex + startMarker.length
        // endMarker 직전까지가 “교체 대상” 영역
        val contentEnd = endIndex

        val before = original.substring(0, contentStart)
        val after = original.substring(contentEnd) // endMarker 포함 뒷부분

        // 3) 마커 사이 내용을 싹 지우고 새 테이블만 넣기
        val newContent =
            buildString {
                append(before)
                appendLine()
                appendLine()
                append(tableMarkdown.trimEnd())
                appendLine()
                append(after)
            }

        docFile.writeText(newContent)

        logger.lifecycle("Updated benchmark doc: ${docFile.path}")
    }
}

// benchmark 끝나면 md 자동 갱신
tasks.matching { it.name == "benchmark" }.configureEach {
    finalizedBy(updateBenchmarkDoc)
}
