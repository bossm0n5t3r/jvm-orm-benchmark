package me.bossm0n5t3r.benchmark

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.TearDown
import me.bossm0n5t3r.benchmark.Constants.DEFAULT_BATCH_SIZE
import me.bossm0n5t3r.benchmark.Constants.DEFAULT_ROWS
import me.bossm0n5t3r.exposed.ExposedApi

@State(Scope.Benchmark)
class ExposedBenchmarks {
    private val api = ExposedApi()

    @Param(DEFAULT_ROWS.toString())
    private var rows: Int = 0

    @Param(DEFAULT_BATCH_SIZE.toString())
    private var batchSize: Int = 0

    @Setup
    fun setup() {
        // 스키마는 ExposedApi.init에서 보장됨
        runCatching { api.deleteAll() }
    }

    @Benchmark
    fun insertBulk() {
        api.insertBulk(count = rows, batchSize = batchSize)
    }

    @TearDown
    fun tearDown() {
        runCatching { api.deleteAll() }
    }
}
