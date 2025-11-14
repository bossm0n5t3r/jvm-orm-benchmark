package me.bossm0n5t3r.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

object DatabaseConfig {
    /**
     * 환경 변수 또는 시스템 프로퍼티로부터 값을 가져옵니다.
     * 우선순위: System.getProperty > System.getenv > 기본값
     */
    fun getProperty(
        name: String,
        env: String,
        default: String,
    ): String =
        System.getProperty(name)
            ?: System.getenv(env)
            ?: default

    /**
     * PostgreSQL 연결 정보를 가져옵니다.
     */
    fun getUrl(): String =
        getProperty(
            name = "pg.url",
            env = "PG_URL",
            default = "jdbc:postgresql://localhost:5432/orm_bench?reWriteBatchedInserts=true",
        )

    fun getUser(): String =
        getProperty(
            name = "pg.user",
            env = "PG_USER",
            default = "postgres",
        )

    fun getPassword(): String =
        getProperty(
            name = "pg.password",
            env = "PG_PASSWORD",
            default = "postgres",
        )

    /**
     * HikariCP DataSource를 생성합니다.
     * 공통 설정을 적용하며, 필요시 poolName을 커스터마이징할 수 있습니다.
     */
    fun createDataSource(poolName: String = "HikariPool"): DataSource {
        val url = getUrl()
        val user = getUser()
        val password = getPassword()

        val config =
            HikariConfig().apply {
                jdbcUrl = url
                driverClassName = "org.postgresql.Driver"
                username = user
                this.password = password
                maximumPoolSize = 8
                minimumIdle = 2
                isAutoCommit = false
                connectionTimeout = 30000
                idleTimeout = 600000
                maxLifetime = 1800000
                this.poolName = poolName
            }
        return HikariDataSource(config)
    }
}
