package io.github.jeongyounghyeon.roe.infrastructure.statemachine

import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.ACCEPT_ORDER
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.CANCEL_REQUEST
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.DELIVER
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.DISPATCH
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.PAY_FAIL
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.PAY_SUCCESS
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.REFUND_SUCCESS
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.REQUEST_PAY
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.CANCELED
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.DELIVERED
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.FAILED
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.PAID
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.PAYMENT_PROCESSING
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.PENDING_PAYMENT
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.PREPARING
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.REFUNDED
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.REFUNDING
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.SHIPPED
import org.springframework.context.annotation.Configuration
import org.springframework.statemachine.config.EnableStateMachineFactory
import org.springframework.statemachine.config.StateMachineConfigurerAdapter
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer

@Configuration
@EnableStateMachineFactory
class OrderStateMachineConfig(
    private val actions: OrderStateMachineActions,
) : StateMachineConfigurerAdapter<OrderStatus, OrderEvent>() {

    override fun configure(states: StateMachineStateConfigurer<OrderStatus, OrderEvent>) {
        states.withStates()
            .initial(PENDING_PAYMENT)
            .end(CANCELED)
            .end(REFUNDED)
            .end(FAILED)
            .states(OrderStatus.entries.toSet())
    }

    override fun configure(transitions: StateMachineTransitionConfigurer<OrderStatus, OrderEvent>) {
        val action = actions.changeStatus
        transitions
            // 정상 주문 플로우
            .withExternal().source(PENDING_PAYMENT).target(PAYMENT_PROCESSING).event(REQUEST_PAY).action(action).and()
            .withExternal().source(PAYMENT_PROCESSING).target(PAID).event(PAY_SUCCESS).action(action).and()
            .withExternal().source(PAYMENT_PROCESSING).target(FAILED).event(PAY_FAIL).action(action).and()
            .withExternal().source(PAID).target(PREPARING).event(ACCEPT_ORDER).action(action).and()
            .withExternal().source(PREPARING).target(SHIPPED).event(DISPATCH).action(action).and()
            .withExternal().source(SHIPPED).target(DELIVERED).event(DELIVER).action(action).and()
            // 취소/환불 플로우 (PAYMENT_PROCESSING, SHIPPED 는 의도적으로 제외)
            .withExternal().source(PENDING_PAYMENT).target(CANCELED).event(CANCEL_REQUEST).action(action).and()
            .withExternal().source(PAID).target(REFUNDING).event(CANCEL_REQUEST).action(action).and()
            .withExternal().source(PREPARING).target(REFUNDING).event(CANCEL_REQUEST).action(action).and()
            .withExternal().source(DELIVERED).target(REFUNDING).event(CANCEL_REQUEST).action(action).and()
            .withExternal().source(REFUNDING).target(REFUNDED).event(REFUND_SUCCESS).action(action)
    }
}