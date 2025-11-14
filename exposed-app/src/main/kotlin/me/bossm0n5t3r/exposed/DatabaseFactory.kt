package me.bossm0n5t3r.exposed

import com.zaxxer.hikari.HikariDataSource
import me.bossm0n5t3r.common.DatabaseConfig
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

    private fun configureDataSource(): HikariDataSource = DatabaseConfig.createDataSource("ExposedHikariPool") as HikariDataSource
}
