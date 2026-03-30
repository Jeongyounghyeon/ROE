package io.github.jeongyounghyeon.roe.application.order

import io.github.jeongyounghyeon.roe.application.lock.DistributedLockManager
import io.github.jeongyounghyeon.roe.application.order.exception.OrderNotFoundException
import io.github.jeongyounghyeon.roe.application.outbox.OrderEventRecordRepository
import io.github.jeongyounghyeon.roe.domain.order.OrderEventRecord
import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import io.github.jeongyounghyeon.roe.domain.order.OrderRepository
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus
import io.github.jeongyounghyeon.roe.domain.order.exception.InvalidOrderStateTransitionException
import io.github.jeongyounghyeon.roe.infrastructure.statemachine.OrderStateMachineActions.Companion.ORDER_HEADER_KEY
import io.github.jeongyounghyeon.roe.infrastructure.statemachine.OrderStateMachineActions.Companion.REASON_HEADER_KEY
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
class OrderCommandService(
    private val orderRepository: OrderRepository,
    private val stateMachineFactory: StateMachineFactory<OrderStatus, OrderEvent>,
    private val lockManager: DistributedLockManager,
    private val orderEventRecordRepository: OrderEventRecordRepository,
) {
    fun createOrder(): Order = orderRepository.save(Order.create())

    @Transactional
    fun processEvent(orderId: UUID, event: OrderEvent, reason: String? = null): Order {
        return lockManager.withLock("order:event:lock:$orderId") {
            val order = orderRepository.findById(orderId)
                ?: throw OrderNotFoundException(orderId)

            val sm = restoreStateMachine(order.status)
            val result = sm.sendEvent(
                Mono.just(
                    MessageBuilder.withPayload(event)
                        .setHeader(ORDER_HEADER_KEY, order)
                        .setHeader(REASON_HEADER_KEY, reason)
                        .build()
                )
            ).blockLast()

            if (result?.resultType != ACCEPTED) {
                throw InvalidOrderStateTransitionException(order.status, event)
            }

            val savedOrder = orderRepository.save(order)
            orderEventRecordRepository.save(
                OrderEventRecord(
                    orderId = savedOrder.id,
                    event = event.name,
                    status = savedOrder.status.name,
                )
            )
            savedOrder
        }
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
