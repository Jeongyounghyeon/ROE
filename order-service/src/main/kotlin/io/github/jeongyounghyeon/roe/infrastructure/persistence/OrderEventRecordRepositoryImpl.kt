package io.github.jeongyounghyeon.roe.infrastructure.persistence

import io.github.jeongyounghyeon.roe.application.outbox.OrderEventRecordRepository
import io.github.jeongyounghyeon.roe.domain.order.OrderEventRecord
import org.springframework.stereotype.Repository

@Repository
class OrderEventRecordRepositoryImpl(
    private val jpaRepository: OrderEventRecordJpaRepository,
) : OrderEventRecordRepository {

    override fun save(record: OrderEventRecord) {
        jpaRepository.save(record)
    }
}
