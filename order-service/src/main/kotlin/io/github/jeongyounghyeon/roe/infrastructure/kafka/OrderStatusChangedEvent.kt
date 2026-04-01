package io.github.jeongyounghyeon.roe.infrastructure.kafka

import java.time.LocalDateTime
import java.util.UUID

data class OrderStatusChangedEvent(
    val orderId: UUID,
    val event: String,
    val status: String,
    val occurredAt: LocalDateTime,
)