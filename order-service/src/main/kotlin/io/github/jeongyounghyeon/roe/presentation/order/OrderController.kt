package io.github.jeongyounghyeon.roe.presentation.order

import io.github.jeongyounghyeon.roe.application.order.OrderCommandService
import io.github.jeongyounghyeon.roe.application.order.OrderQueryService
import io.github.jeongyounghyeon.roe.presentation.order.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderCommandService: OrderCommandService,
    private val orderQueryService: OrderQueryService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(): OrderResponse =
        orderCommandService.createOrder().toResponse()

    @PostMapping("/{id}/events")
    fun processEvent(
        @PathVariable id: UUID,
        @RequestBody @Valid request: ProcessEventRequest,
    ): OrderResponse =
        orderCommandService.processEvent(id, request.event, request.reason).toResponse()

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: UUID): OrderResponse =
        orderQueryService.getOrder(id).toResponse()
}
