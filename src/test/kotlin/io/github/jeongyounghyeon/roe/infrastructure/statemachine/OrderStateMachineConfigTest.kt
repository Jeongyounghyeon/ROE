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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.support.MessageBuilder
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.StateMachineEventResult.ResultType.ACCEPTED
import org.springframework.statemachine.StateMachineEventResult.ResultType.DENIED
import org.springframework.statemachine.config.StateMachineFactory
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono

@SpringBootTest
@ActiveProfiles("test")
class OrderStateMachineConfigTest {

    @Autowired
    lateinit var stateMachineFactory: StateMachineFactory<OrderStatus, OrderEvent>

    private fun createStateMachine(): StateMachine<OrderStatus, OrderEvent> =
        stateMachineFactory.getStateMachine().also { it.startReactively().block() }

    private fun StateMachine<OrderStatus, OrderEvent>.send(event: OrderEvent) =
        sendEvent(Mono.just(MessageBuilder.withPayload(event).build())).blockLast()

    // ── 초기 상태 ───────────────────────────────────────────────

    @Test
    fun `초기 상태는 PENDING_PAYMENT 이어야 한다`() {
        val sm = createStateMachine()
        assertThat(sm.state.id).isEqualTo(PENDING_PAYMENT)
    }

    // ── 정상 주문 플로우 ─────────────────────────────────────────

    @Test
    fun `PENDING_PAYMENT 에서 REQUEST_PAY 이벤트로 PAYMENT_PROCESSING 전이`() {
        val sm = createStateMachine()
        assertThat(sm.send(REQUEST_PAY)?.resultType).isEqualTo(ACCEPTED)
        assertThat(sm.state.id).isEqualTo(PAYMENT_PROCESSING)
    }

    @Test
    fun `PAYMENT_PROCESSING 에서 PAY_SUCCESS 이벤트로 PAID 전이`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY)
        assertThat(sm.send(PAY_SUCCESS)?.resultType).isEqualTo(ACCEPTED)
        assertThat(sm.state.id).isEqualTo(PAID)
    }

    @Test
    fun `PAYMENT_PROCESSING 에서 PAY_FAIL 이벤트로 FAILED 전이`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY)
        assertThat(sm.send(PAY_FAIL)?.resultType).isEqualTo(ACCEPTED)
        assertThat(sm.state.id).isEqualTo(FAILED)
    }

    @Test
    fun `PAID 에서 ACCEPT_ORDER 이벤트로 PREPARING 전이`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY); sm.send(PAY_SUCCESS)
        assertThat(sm.send(ACCEPT_ORDER)?.resultType).isEqualTo(ACCEPTED)
        assertThat(sm.state.id).isEqualTo(PREPARING)
    }

    @Test
    fun `PREPARING 에서 DISPATCH 이벤트로 SHIPPED 전이`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY); sm.send(PAY_SUCCESS); sm.send(ACCEPT_ORDER)
        assertThat(sm.send(DISPATCH)?.resultType).isEqualTo(ACCEPTED)
        assertThat(sm.state.id).isEqualTo(SHIPPED)
    }

    @Test
    fun `SHIPPED 에서 DELIVER 이벤트로 DELIVERED 전이`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY); sm.send(PAY_SUCCESS); sm.send(ACCEPT_ORDER); sm.send(DISPATCH)
        assertThat(sm.send(DELIVER)?.resultType).isEqualTo(ACCEPTED)
        assertThat(sm.state.id).isEqualTo(DELIVERED)
    }

    // ── 취소/환불 플로우 ─────────────────────────────────────────

    @Test
    fun `PENDING_PAYMENT 에서 CANCEL_REQUEST 이벤트로 CANCELED 전이`() {
        val sm = createStateMachine()
        assertThat(sm.send(CANCEL_REQUEST)?.resultType).isEqualTo(ACCEPTED)
        assertThat(sm.state.id).isEqualTo(CANCELED)
    }

    @Test
    fun `PAID 에서 CANCEL_REQUEST 이벤트로 REFUNDING 전이`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY); sm.send(PAY_SUCCESS)
        assertThat(sm.send(CANCEL_REQUEST)?.resultType).isEqualTo(ACCEPTED)
        assertThat(sm.state.id).isEqualTo(REFUNDING)
    }

    @Test
    fun `PREPARING 에서 CANCEL_REQUEST 이벤트로 REFUNDING 전이`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY); sm.send(PAY_SUCCESS); sm.send(ACCEPT_ORDER)
        assertThat(sm.send(CANCEL_REQUEST)?.resultType).isEqualTo(ACCEPTED)
        assertThat(sm.state.id).isEqualTo(REFUNDING)
    }

    @Test
    fun `DELIVERED 에서 CANCEL_REQUEST 이벤트로 REFUNDING 전이`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY); sm.send(PAY_SUCCESS); sm.send(ACCEPT_ORDER); sm.send(DISPATCH); sm.send(DELIVER)
        assertThat(sm.send(CANCEL_REQUEST)?.resultType).isEqualTo(ACCEPTED)
        assertThat(sm.state.id).isEqualTo(REFUNDING)
    }

    @Test
    fun `REFUNDING 에서 REFUND_SUCCESS 이벤트로 REFUNDED 전이`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY); sm.send(PAY_SUCCESS); sm.send(CANCEL_REQUEST)
        assertThat(sm.send(REFUND_SUCCESS)?.resultType).isEqualTo(ACCEPTED)
        assertThat(sm.state.id).isEqualTo(REFUNDED)
    }

    // ── 취소 불가 상태 검증 ──────────────────────────────────────

    @Test
    fun `PAYMENT_PROCESSING 에서 CANCEL_REQUEST 는 거부되어야 한다`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY)
        assertThat(sm.send(CANCEL_REQUEST)?.resultType).isEqualTo(DENIED)
        assertThat(sm.state.id).isEqualTo(PAYMENT_PROCESSING)
    }

    @Test
    fun `SHIPPED 에서 CANCEL_REQUEST 는 거부되어야 한다`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY); sm.send(PAY_SUCCESS); sm.send(ACCEPT_ORDER); sm.send(DISPATCH)
        assertThat(sm.send(CANCEL_REQUEST)?.resultType).isEqualTo(DENIED)
        assertThat(sm.state.id).isEqualTo(SHIPPED)
    }

    // ── 종결 상태 이후 전이 불가 검증 ────────────────────────────

    @Test
    fun `CANCELED 종결 상태에서 추가 전이는 거부되어야 한다`() {
        val sm = createStateMachine()
        sm.send(CANCEL_REQUEST)
        assertThat(sm.send(REQUEST_PAY)?.resultType).isEqualTo(DENIED)
        assertThat(sm.state.id).isEqualTo(CANCELED)
    }

    @Test
    fun `FAILED 종결 상태에서 추가 전이는 거부되어야 한다`() {
        val sm = createStateMachine()
        sm.send(REQUEST_PAY); sm.send(PAY_FAIL)
        assertThat(sm.send(REQUEST_PAY)?.resultType).isEqualTo(DENIED)
        assertThat(sm.state.id).isEqualTo(FAILED)
    }
}