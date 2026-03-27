package io.github.jeongyounghyeon.roe.application.event

import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent

interface OrderDomainEventPublisher {
    fun publish(order: Order, event: OrderEvent)
}
