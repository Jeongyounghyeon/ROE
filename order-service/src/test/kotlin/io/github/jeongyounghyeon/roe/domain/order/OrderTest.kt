package io.github.jeongyounghyeon.roe.domain.order

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OrderTest {

    @Test
    fun `주문 생성 시 PENDING_PAYMENT 상태여야 한다`() {
        val order = Order.create()

        assertThat(order.status).isEqualTo(OrderStatus.PENDING_PAYMENT)
    }

    @Test
    fun `주문 생성 시 생성 이력 1건이 기록되어야 한다`() {
        val order = Order.create()

        assertThat(order.histories).hasSize(1)
    }

    @Test
    fun `주문 생성 이력은 이전 상태 없이 PENDING_PAYMENT 로 기록되어야 한다`() {
        val order = Order.create()
        val history = order.histories.first()

        assertThat(history.event).isNull()
        assertThat(history.fromStatus).isNull()
        assertThat(history.toStatus).isEqualTo(OrderStatus.PENDING_PAYMENT)
    }

    @Test
    fun `상태 변경 시 변경된 상태로 업데이트되어야 한다`() {
        val order = Order.create()

        order.changeStatus(OrderEvent.REQUEST_PAY, OrderStatus.PAYMENT_PROCESSING)

        assertThat(order.status).isEqualTo(OrderStatus.PAYMENT_PROCESSING)
    }

    @Test
    fun `상태 변경 시 이력이 추가로 기록되어야 한다`() {
        val order = Order.create()

        order.changeStatus(OrderEvent.REQUEST_PAY, OrderStatus.PAYMENT_PROCESSING)

        assertThat(order.histories).hasSize(2)
    }

    @Test
    fun `상태 변경 이력에 이전 상태, 이후 상태, 이벤트가 기록되어야 한다`() {
        val order = Order.create()

        order.changeStatus(OrderEvent.REQUEST_PAY, OrderStatus.PAYMENT_PROCESSING)
        val history = order.histories.last()

        assertThat(history.event).isEqualTo(OrderEvent.REQUEST_PAY)
        assertThat(history.fromStatus).isEqualTo(OrderStatus.PENDING_PAYMENT)
        assertThat(history.toStatus).isEqualTo(OrderStatus.PAYMENT_PROCESSING)
    }

    @Test
    fun `상태 변경 시 사유가 이력에 기록되어야 한다`() {
        val order = Order.create()
        order.changeStatus(OrderEvent.REQUEST_PAY, OrderStatus.PAYMENT_PROCESSING)
        order.changeStatus(OrderEvent.PAY_SUCCESS, OrderStatus.PAID)

        order.changeStatus(OrderEvent.CANCEL_REQUEST, OrderStatus.REFUNDING, reason = "고객 변심")
        val history = order.histories.last()

        assertThat(history.reason).isEqualTo("고객 변심")
    }

    @Test
    fun `연속 상태 변경 시 이력이 순서대로 누적되어야 한다`() {
        val order = Order.create()

        order.changeStatus(OrderEvent.REQUEST_PAY, OrderStatus.PAYMENT_PROCESSING)
        order.changeStatus(OrderEvent.PAY_SUCCESS, OrderStatus.PAID)
        order.changeStatus(OrderEvent.ACCEPT_ORDER, OrderStatus.PREPARING)

        assertThat(order.histories).hasSize(4)
        assertThat(order.histories.map { it.toStatus }).containsExactly(
            OrderStatus.PENDING_PAYMENT,
            OrderStatus.PAYMENT_PROCESSING,
            OrderStatus.PAID,
            OrderStatus.PREPARING,
        )
    }
}