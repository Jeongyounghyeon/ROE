package io.github.jeongyounghyeon.roe.infrastructure.persistence

import io.github.jeongyounghyeon.roe.domain.order.OrderEventRecord
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderEventRecordJpaRepository : JpaRepository<OrderEventRecord, UUID> {
    fun findByPublishedAtIsNull(): List<OrderEventRecord>
    fun countByPublishedAtIsNull(): Long
    fun findByOrderId(orderId: UUID): List<OrderEventRecord>
}
