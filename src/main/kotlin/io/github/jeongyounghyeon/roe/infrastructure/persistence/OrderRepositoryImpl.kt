package io.github.jeongyounghyeon.roe.infrastructure.persistence

import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class OrderRepositoryImpl(
    private val jpaRepository: OrderJpaRepository,
) : OrderRepository {

    override fun save(order: Order): Order = jpaRepository.save(order)

    override fun findById(id: UUID): Order? = jpaRepository.findById(id).orElse(null)
}
