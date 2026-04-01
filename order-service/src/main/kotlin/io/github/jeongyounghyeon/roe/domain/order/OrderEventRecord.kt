package io.github.jeongyounghyeon.roe.domain.order

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "order_event_records")
class OrderEventRecord(
    val orderId: UUID,
    val event: String,
    val status: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var publishedAt: LocalDateTime? = null,

    @Id
    val id: UUID = UUID.randomUUID(),
) {
    fun markPublished() {
        publishedAt = LocalDateTime.now()
    }
}