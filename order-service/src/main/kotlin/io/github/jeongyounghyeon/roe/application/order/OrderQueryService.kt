package io.github.jeongyounghyeon.roe.application.order

import io.github.jeongyounghyeon.roe.application.order.exception.OrderNotFoundException
import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrderQueryService(
    private val orderRepository: OrderRepository,
) {
    fun getOrder(id: UUID): Order =
        orderRepository.findById(id) ?: throw OrderNotFoundException(id)
}
