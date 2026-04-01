package io.github.jeongyounghyeon.roe.domain.order

import java.util.UUID

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: UUID): Order?
}