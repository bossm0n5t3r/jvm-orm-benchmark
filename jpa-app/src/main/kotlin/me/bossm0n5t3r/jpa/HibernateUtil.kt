package me.bossm0n5t3r.jpa

import me.bossm0n5t3r.common.DatabaseConfig
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.AvailableSettings

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

    private fun configureDataSource() = DatabaseConfig.createDataSource("JpaHikariPool")

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
