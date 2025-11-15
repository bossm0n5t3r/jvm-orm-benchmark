package me.bossm0n5t3r.exposed

import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDateTime

class ExposedApi {
    init {
        // Ensure DB is connected and the schema exists
        DatabaseFactory.connect()
        transaction {
            SchemaUtils.create(Users)
        }
    }

    fun ping(): String = "exposed-app ready"

    /**
     * 100만 건 이상의 데이터를 대량 삽입합니다.
     * @param count 기본 1,000,000건. 더 큰 값도 가능합니다.
     * @param batchSize JDBC/Exposed 배치 크기. 너무 크면 메모리 사용량이 늘어납니다.
     */
    fun insertBulk(
        count: Int,
        batchSize: Int,
    ) {
        require(count > 0) { "count must be > 0" }
        require(batchSize in 1..100_000) { "batchSize must be between 1 and 100_000" }

        val started = System.nanoTime()
        var inserted = 0

        // 트랜잭션 경계는 배치 크기 단위로 나눠 메모리 사용을 제한합니다.
        while (inserted < count) {
            val remaining = count - inserted
            val chunk = minOf(remaining, batchSize)

            transaction {
                // 성능을 위해 auto-commit은 풀(Hikari)에서 false, 여기선 트랜잭션 커밋으로 처리
                Users.batchInsert((0 until chunk)) { i ->
                    val seq = inserted + i + 1
                    this[Users.username] = "user_${seq.toString().padStart(7, '0')}"
                    this[Users.email] = "user$seq@example.com"
                    this[Users.createdAt] = LocalDateTime.now().toKotlinLocalDateTime()
                }
            }

            inserted += chunk
        }

        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        // 간단한 로그 출력
        println("[exposed-app] insertBulk completed: count=$count, batchSize=$batchSize, elapsedMs=$elapsedMs")
    }

    /**
     * 전체 삭제. 가능하면 TRUNCATE로 더 빠르게 수행하고, 실패 시 deleteAll로 폴백합니다.
     */
    fun deleteAll() {
        val started = System.nanoTime()
        transaction {
            Users.deleteAll()
        }
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        println("[exposed-app] deleteAll completed in ${elapsedMs}ms")
    }
}
