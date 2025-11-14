package me.bossm0n5t3r.exposed

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import javax.sql.DataSource

object DatabaseFactory {
    @Volatile private var initialized: Boolean = false

    fun connect() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val dataSource = configureDataSource()
            Database.connect(dataSource)
            initialized = true
        }
    }

    private fun configureDataSource(): DataSource {
        fun prop(
            name: String,
            env: String,
            default: String,
        ): String = System.getProperty(name) ?: System.getenv(env) ?: default

        val url =
            prop(
                name = "pg.url",
                env = "PG_URL",
                default = "jdbc:postgresql://localhost:5432/jpa_bench?reWriteBatchedInserts=true",
            )
        val user = prop(name = "pg.user", env = "PG_USER", default = "postgres")
        val pass = prop(name = "pg.password", env = "PG_PASSWORD", default = "postgres")

        val config =
            HikariConfig().apply {
                jdbcUrl = url
                driverClassName = "org.postgresql.Driver"
                username = user
                password = pass
                maximumPoolSize = 8
                minimumIdle = 2
                isAutoCommit = false
            }
        return HikariDataSource(config)
    }
}
