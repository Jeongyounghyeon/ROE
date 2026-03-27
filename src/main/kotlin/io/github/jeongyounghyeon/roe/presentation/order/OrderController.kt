package io.github.jeongyounghyeon.roe.presentation.order

import io.github.jeongyounghyeon.roe.application.order.OrderCommandService
import io.github.jeongyounghyeon.roe.application.order.OrderQueryService
import io.github.jeongyounghyeon.roe.presentation.order.dto.OrderResponse
import io.github.jeongyounghyeon.roe.presentation.order.dto.ProcessEventRequest
import io.github.jeongyounghyeon.roe.presentation.order.dto.toResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
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
