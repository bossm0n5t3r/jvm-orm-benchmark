package me.bossm0n5t3r.exposed

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

/**
 * JPA의 UserEntity와 동일한 스키마를 갖는 Exposed 테이블 정의
 * - 테이블명: users
 * - 컬럼: id (IDENTITY), username(varchar 100 not null), email(varchar 200 not null), createdAt(timestamp with time zone not null)
 */
object Users : Table(name = "users") {
    val id = long("id").autoIncrement()
    val username = varchar("username", length = 100)
    val email = varchar("email", length = 200)
    val createdAt = datetime("createdAt")

    override val primaryKey = PrimaryKey(id)
}
