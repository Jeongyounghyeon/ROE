package io.github.jeongyounghyeon.roe.domain.order.exception

import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus

class InvalidOrderStateTransitionException(
    currentStatus: OrderStatus,
    event: OrderEvent,
) : IllegalStateException("Cannot apply event '$event' in state '$currentStatus'")