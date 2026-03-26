package io.github.jeongyounghyeon.roe.infrastructure.statemachine

import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus
import org.springframework.statemachine.action.Action
import org.springframework.stereotype.Component

@Component
class OrderStateMachineActions {

    val changeStatus = Action<OrderStatus, OrderEvent> { context ->
        val order = context.messageHeaders[ORDER_HEADER_KEY] as? Order ?: return@Action
        val event = context.event ?: return@Action
        order.changeStatus(event, context.target.id)
    }

    companion object {
        const val ORDER_HEADER_KEY = "order"
    }
}
