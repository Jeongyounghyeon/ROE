package io.github.jeongyounghyeon.roe.presentation.order.dto

import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus
import java.time.LocalDateTime
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    val histories: List<OrderHistoryResponse>,
)

fun Order.toResponse() = OrderResponse(
    id = id,
    status = status,
    createdAt = createdAt,
    histories = histories.map { it.toResponse() },
)
