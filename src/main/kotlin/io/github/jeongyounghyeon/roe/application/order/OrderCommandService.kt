package io.github.jeongyounghyeon.roe.application.order

import io.github.jeongyounghyeon.roe.application.order.exception.OrderNotFoundException
import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import io.github.jeongyounghyeon.roe.domain.order.OrderRepository
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus
import io.github.jeongyounghyeon.roe.domain.order.exception.InvalidOrderStateTransitionException
import io.github.jeongyounghyeon.roe.infrastructure.statemachine.OrderStateMachineActions.Companion.ORDER_HEADER_KEY
import org.springframework.messaging.support.MessageBuilder
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.StateMachineEventResult.ResultType.ACCEPTED
import org.springframework.statemachine.config.StateMachineFactory
import org.springframework.statemachine.support.DefaultStateMachineContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.util.UUID

@Service
@Transactional
class OrderCommandService(
    private val orderRepository: OrderRepository,
    private val stateMachineFactory: StateMachineFactory<OrderStatus, OrderEvent>,
) {
    fun createOrder(): Order {
        val order = Order.create()
        return orderRepository.save(order)
    }

    fun processEvent(orderId: UUID, event: OrderEvent): Order {
        val order = orderRepository.findById(orderId)
            ?: throw OrderNotFoundException(orderId)

        val sm = restoreStateMachine(order.status)
        val result = sm.sendEvent(
            Mono.just(
                MessageBuilder.withPayload(event)
                    .setHeader(ORDER_HEADER_KEY, order)
                    .build()
            )
        ).blockLast()

        if (result?.resultType != ACCEPTED) {
            throw InvalidOrderStateTransitionException(order.status, event)
        }

        return orderRepository.save(order)
    }

    private fun restoreStateMachine(currentStatus: OrderStatus): StateMachine<OrderStatus, OrderEvent> {
        val sm = stateMachineFactory.getStateMachine()
        sm.stateMachineAccessor.doWithAllRegions { accessor ->
            accessor.resetStateMachineReactively(
                DefaultStateMachineContext(currentStatus, null, null, null)
            ).block()
        }
        sm.startReactively().block()
        return sm
    }
}
