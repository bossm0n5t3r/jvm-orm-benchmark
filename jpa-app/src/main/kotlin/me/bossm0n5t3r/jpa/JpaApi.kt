package me.bossm0n5t3r.jpa

import org.hibernate.StatelessSession
import org.hibernate.Transaction

class JpaApi {
    fun ping(): String = "jpa-app ready"

    /**
     * 대량(예: 1,000,000건 이상) 사용자 데이터를 삽입합니다.
     * 성능을 위해 Hibernate StatelessSession + JDBC 배치를 사용합니다.
     * @param count 생성/저장할 사용자 수
     * @param batchSize 배치 크기 (기본 1000)
     * @return 실제로 저장 시도한 레코드 수
     */
    fun insertUsers(
        count: Int,
        batchSize: Int = 1_000,
    ): Int {
        require(count > 0) { "count must be > 0" }
        require(batchSize > 0) { "batchSize must be > 0" }

        val sf = HibernateUtil.sessionFactory
        val start = System.nanoTime()

        var inserted = 0
        var session: StatelessSession? = null
        var tx: Transaction? = null

        try {
            session = sf.openStatelessSession()
            tx = session.beginTransaction()

            for (i in 1..count) {
                val user =
                    UserEntity(
                        username = "user_$i",
                        email = "user_$i@example.com",
                    )
                session.insert(user)
                inserted++

                if (inserted % batchSize == 0) {
                    // StatelessSession은 1차 캐시가 없어 flush/clear 불필요하지만
                    // 주기적으로 트랜잭션을 커밋/리뉴얼하여 메모리 압박을 낮출 수 있습니다.
                    tx?.commit()
                    tx = session.beginTransaction()
                }
            }

            tx?.commit()
        } catch (e: Exception) {
            try {
                tx?.rollback()
            } catch (_: Exception) {
            }
            throw e
        } finally {
            session?.close()
        }

        val tookMs = (System.nanoTime() - start) / 1_000_000
        println("[JPA] insertUsers count=$count batchSize=$batchSize took=${tookMs}ms")
        return inserted
    }

    /**
     * 모든 사용자 레코드를 벌크 삭제합니다.
     * @return 삭제된 레코드 수
     */
    fun deleteAllUsers(): Int {
        val sf = HibernateUtil.sessionFactory
        val start = System.nanoTime()

        sf.openSession().use { session ->
            val tx = session.beginTransaction()
            try {
                val deleted = session.createMutationQuery("delete from UserEntity").executeUpdate()
                tx.commit()

                val tookMs = (System.nanoTime() - start) / 1_000_000
                println("[JPA] deleteAllUsers deleted=$deleted took=${tookMs}ms")
                return deleted
            } catch (e: Exception) {
                try {
                    tx.rollback()
                } catch (_: Exception) {
                }
                throw e
            }
        }
    }
}
