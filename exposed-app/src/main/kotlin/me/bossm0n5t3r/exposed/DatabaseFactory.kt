package me.bossm0n5t3r.exposed

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database

object DatabaseFactory {
    @Volatile private var initialized: Boolean = false
    private var dataSource: HikariDataSource? = null

    fun connect() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            try {
                val ds = configureDataSource()
                Database.connect(ds)
                dataSource = ds
                initialized = true
                println("[DatabaseFactory] Database connected successfully")
            } catch (e: Exception) {
                println("[DatabaseFactory] Failed to connect to database: ${e.message}")
                throw e
            }
        }
    }

    fun close() {
        synchronized(this) {
            try {
                dataSource?.close()
                println("[DatabaseFactory] Database connection closed")
            } catch (e: Exception) {
                println("[DatabaseFactory] Error closing database connection: ${e.message}")
            } finally {
                dataSource = null
                initialized = false
            }
        }
    }

    private fun configureDataSource(): HikariDataSource {
        fun prop(
            name: String,
            env: String,
            default: String,
        ): String = System.getProperty(name) ?: System.getenv(env) ?: default

        val url =
            prop(
                name = "pg.url",
                env = "PG_URL",
                default = "jdbc:postgresql://localhost:5432/orm_bench?reWriteBatchedInserts=true",
            )
        val user = prop(name = "pg.user", env = "PG_USER", default = "postgres")
        val pass = prop(name = "pg.password", env = "PG_PASSWORD", default = "postgres")

        require(url.isNotBlank()) { "Database URL must not be blank" }
        require(user.isNotBlank()) { "Database user must not be blank" }

        val config =
            HikariConfig().apply {
                jdbcUrl = url
                driverClassName = "org.postgresql.Driver"
                username = user
                password = pass
                maximumPoolSize = 8
                minimumIdle = 2
                isAutoCommit = false
                connectionTimeout = 30_000 // 30초
                idleTimeout = 600_000 // 10분
                maxLifetime = 1_800_000 // 30분
                poolName = "ExposedHikariPool"

                // 커넥션 테스트 쿼리 (선택사항)
                connectionTestQuery = "SELECT 1"
            }
        return HikariDataSource(config)
    }
}
