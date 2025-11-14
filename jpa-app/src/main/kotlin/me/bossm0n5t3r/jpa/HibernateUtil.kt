package me.bossm0n5t3r.jpa

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.AvailableSettings
import javax.sql.DataSource

object HibernateUtil {
    val sessionFactory: SessionFactory by lazy { buildSessionFactory() }

    private fun buildSessionFactory(): SessionFactory =
        try {
            val dataSource = configureDataSource()

            val registryBuilder =
                StandardServiceRegistryBuilder()
                    .applySetting(AvailableSettings.DATASOURCE, dataSource)
                    .applySetting(AvailableSettings.HBM2DDL_AUTO, "update")
                    .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
                    .applySetting(AvailableSettings.SHOW_SQL, false)
                    .applySetting(AvailableSettings.FORMAT_SQL, false)
                    .applySetting(AvailableSettings.GENERATE_STATISTICS, false)
                    // JDBC batch & ordering for bulk operations
                    .applySetting(AvailableSettings.STATEMENT_BATCH_SIZE, 1000)
                    .applySetting(AvailableSettings.ORDER_INSERTS, true)
                    .applySetting(AvailableSettings.ORDER_UPDATES, true)

            val registry = registryBuilder.build()

            MetadataSources(registry)
                .addAnnotatedClass(UserEntity::class.java)
                .buildMetadata()
                .buildSessionFactory()
        } catch (e: Exception) {
            throw RuntimeException("Failed to build SessionFactory", e)
        }

    private fun configureDataSource(): DataSource {
        // 환경 변수 또는 시스템 프로퍼티로 PostgreSQL 접속 정보를 받습니다.
        // 우선순위: System.getProperty > System.getenv > 기본값
        fun prop(
            name: String,
            env: String,
            default: String,
        ): String =
            System.getProperty(name)
                ?: System.getenv(env)
                ?: default

        val url =
            prop(
                name = "pg.url",
                env = "PG_URL",
                default = "jdbc:postgresql://localhost:5432/orm_bench?reWriteBatchedInserts=true",
            )
        val user =
            prop(
                name = "pg.user",
                env = "PG_USER",
                default = "postgres",
            )
        val pass =
            prop(
                name = "pg.password",
                env = "PG_PASSWORD",
                default = "postgres",
            )

        val config =
            HikariConfig().apply {
                jdbcUrl = url
                driverClassName = "org.postgresql.Driver"
                username = user
                password = pass
                maximumPoolSize = 8
                minimumIdle = 2
                isAutoCommit = false
                // 성능 최적화 옵션
                connectionTimeout = 30000
                idleTimeout = 600000
                maxLifetime = 1800000
            }
        return HikariDataSource(config)
    }

    /**
     * SessionFactory와 DataSource를 정리합니다.
     * 애플리케이션 종료 시 호출하세요.
     */
    fun close() {
        if (sessionFactory.isOpen) {
            sessionFactory.close()
        }
    }
}
