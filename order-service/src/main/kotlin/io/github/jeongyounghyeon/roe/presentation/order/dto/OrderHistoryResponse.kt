package io.github.jeongyounghyeon.roe.presentation.order.dto

import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import io.github.jeongyounghyeon.roe.domain.order.OrderHistory
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus
import java.time.LocalDateTime

data class OrderHistoryResponse(
    val event: OrderEvent?,
    val fromStatus: OrderStatus?,
    val toStatus: OrderStatus,
    val reason: String?,
    val occurredAt: LocalDateTime,
)

fun OrderHistory.toResponse() = OrderHistoryResponse(
    event = event,
    fromStatus = fromStatus,
    toStatus = toStatus,
    reason = reason,
    occurredAt = occurredAt,
)
