package me.bossm0n5t3r.benchmark

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.TearDown
import me.bossm0n5t3r.jpa.JpaApi

@State(Scope.Benchmark)
class JpaBenchmarks {
    private val api = JpaApi()

    @Param("1000", "10000")
    private var rows: Int = 1000

    @Param("500", "1000", "5000")
    private var batchSize: Int = 1000

    @Setup
    fun setup() {
        // 준비 단계에서 테이블 비우기
        runCatching { api.deleteAllUsers() }
    }

    @Benchmark
    fun insertUsers() {
        api.insertUsers(count = rows, batchSize = batchSize)
    }

    @TearDown
    fun tearDown() {
        // 벤치마크 후 정리
        runCatching { api.deleteAllUsers() }
    }
}
