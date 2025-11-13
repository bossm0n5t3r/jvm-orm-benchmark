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

    private fun buildSessionFactory(): SessionFactory {
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
                .applySetting(AvailableSettings.BATCH_VERSIONED_DATA, true)

        val registry = registryBuilder.build()

        return MetadataSources(registry)
            .addAnnotatedClass(UserEntity::class.java)
            .buildMetadata()
            .buildSessionFactory()
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
                default = "jdbc:postgresql://localhost:5432/jpa_bench?reWriteBatchedInserts=true",
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
            }
        return HikariDataSource(config)
    }
}
