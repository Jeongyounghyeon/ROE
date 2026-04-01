package io.github.jeongyounghyeon.roe.presentation.order.dto

import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import jakarta.validation.constraints.NotNull

data class ProcessEventRequest(
    @field:NotNull
    val event: OrderEvent,
    val reason: String? = null,
)