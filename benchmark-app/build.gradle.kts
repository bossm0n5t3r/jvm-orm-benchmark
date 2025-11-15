import groovy.json.JsonSlurper
import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
            warmups = 1
            iterations = 1
            iterationTime = 3
            iterationTimeUnit = "ns"
            mode = "AverageTime"
            reportFormat = "json"
            reportsDir = "$rootDir/benchmark-reports"
        }
    }
}

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

        val parentDirName = reportFile.parentFile?.name ?: ""
        val timestamp: String =
            run {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH.mm.ss.SSSSSS")
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                val dt =
                    runCatching {
                        LocalDateTime.parse(parentDirName, inputFormatter)
                    }.getOrElse {
                        LocalDateTime.now()
                    }

                dt.format(outputFormatter)
            }

        val headerHtml =
            """
            <table>
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Benchmark</th>
                <th>Benchmark Class</th>
                <th>batchSize</th>
                <th>rows</th>
                <th>Score (s/op)</th>
                <th>Threads</th>
                <th>Forks</th>
                <th>JDK</th>
                <th>Warmup Iters</th>
                <th>Warmup Time</th>
                <th>Warmup Batch</th>
              </tr>
            </thead>
            <tbody>
            """.trimIndent()

        val rowsHtml =
            parsed.joinToString("\n") { bm ->
                val fullName = bm["benchmark"] as? String ?: "unknown"

                val methodName = fullName.substringAfterLast('.')
                val classFqName = fullName.substringBeforeLast('.', "unknown")
                val classSimpleName = classFqName.substringAfterLast('.', classFqName)

                val params = bm["params"] as? Map<*, *> ?: emptyMap<Any, Any>()
                val batchSize = params["batchSize"] ?: "-"
                val rowsParam = params["rows"] ?: "-"

                val primary = bm["primaryMetric"] as? Map<*, *>
                val score = primary?.get("score") ?: "-"

                val threads = bm["threads"] ?: "-"
                val forks = bm["forks"] ?: "-"
                val jdkVersion = bm["jdkVersion"] ?: "-"

                val warmupIterations = bm["warmupIterations"] ?: "-"
                val warmupTime = bm["warmupTime"] ?: "-"
                val warmupBatchSize = bm["warmupBatchSize"] ?: "-"

                """
                <tr>
                  <td>$timestamp</td>
                  <td>$methodName</td>
                  <td>$classSimpleName</td>
                  <td>$batchSize</td>
                  <td>$rowsParam</td>
                  <td>$score</td>
                  <td>$threads</td>
                  <td>$forks</td>
                  <td>$jdkVersion</td>
                  <td>$warmupIterations</td>
                  <td>$warmupTime</td>
                  <td>$warmupBatchSize</td>
                </tr>
                """.trimIndent()
            }

        val footerHtml =
            """
            </tbody>
            </table>
            """.trimIndent()

        val tableHtml = headerHtml + "\n" + rowsHtml + "\n" + footerHtml + "\n"

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

        val contentStart = startIndex + startMarker.length
        val contentEnd = endIndex

        val before = original.take(contentStart)
        val after = original.substring(contentEnd)

        val newContent =
            buildString {
                append(before)
                appendLine()
                appendLine()
                append(tableHtml.trimEnd())
                appendLine()
                append(after)
            }

        docFile.writeText(newContent)

        logger.lifecycle("Updated benchmark doc with HTML table: ${docFile.path}")

        val rootBenchmarkReports = File(rootProject.projectDir, "benchmark-reports")

        if (rootBenchmarkReports.exists()) {
            val deleted = rootBenchmarkReports.deleteRecursively()
            if (deleted) {
                logger.lifecycle("Deleted benchmark-reports directory: ${rootBenchmarkReports.path}")
            } else {
                logger.warn("Failed to delete benchmark-reports directory: ${rootBenchmarkReports.path}")
            }
        }
    }
}

tasks.matching { it.name == "benchmark" }.configureEach {
    finalizedBy(updateBenchmarkDoc)
}
