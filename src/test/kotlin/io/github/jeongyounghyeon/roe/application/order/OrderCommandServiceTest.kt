package io.github.jeongyounghyeon.roe.application.order

import io.github.jeongyounghyeon.roe.application.order.exception.OrderNotFoundException
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.CANCEL_REQUEST
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.PAY_SUCCESS
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.REQUEST_PAY
import io.github.jeongyounghyeon.roe.domain.order.OrderRepository
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.PAID
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.PAYMENT_PROCESSING
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.PENDING_PAYMENT
import io.github.jeongyounghyeon.roe.domain.order.exception.InvalidOrderStateTransitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class OrderCommandServiceTest {

    @Autowired
    lateinit var orderCommandService: OrderCommandService

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Test
    fun `주문 생성 시 PENDING_PAYMENT 상태로 저장되어야 한다`() {
        val order = orderCommandService.createOrder()

        assertThat(order.status).isEqualTo(PENDING_PAYMENT)
        assertThat(orderRepository.findById(order.id)).isNotNull
    }

    @Test
    fun `유효한 이벤트 처리 시 상태가 전이되어야 한다`() {
        val order = orderCommandService.createOrder()

        val updated = orderCommandService.processEvent(order.id, REQUEST_PAY)

        assertThat(updated.status).isEqualTo(PAYMENT_PROCESSING)
    }

    @Test
    fun `연속 이벤트 처리 시 상태가 순차적으로 전이되어야 한다`() {
        val order = orderCommandService.createOrder()

        orderCommandService.processEvent(order.id, REQUEST_PAY)
        val updated = orderCommandService.processEvent(order.id, PAY_SUCCESS)

        assertThat(updated.status).isEqualTo(PAID)
    }

    @Test
    fun `상태 전이 시 이력이 기록되어야 한다`() {
        val order = orderCommandService.createOrder()

        orderCommandService.processEvent(order.id, REQUEST_PAY)
        val updated = orderRepository.findById(order.id)!!

        assertThat(updated.histories).hasSize(2)
        assertThat(updated.histories.last().event).isEqualTo(REQUEST_PAY)
        assertThat(updated.histories.last().fromStatus).isEqualTo(PENDING_PAYMENT)
        assertThat(updated.histories.last().toStatus).isEqualTo(PAYMENT_PROCESSING)
    }

    @Test
    fun `PAYMENT_PROCESSING 상태에서 CANCEL_REQUEST 시 예외가 발생해야 한다`() {
        val order = orderCommandService.createOrder()
        orderCommandService.processEvent(order.id, REQUEST_PAY)

        assertThatThrownBy { orderCommandService.processEvent(order.id, CANCEL_REQUEST) }
            .isInstanceOf(InvalidOrderStateTransitionException::class.java)
    }

    @Test
    fun `존재하지 않는 주문 이벤트 처리 시 예외가 발생해야 한다`() {
        assertThatThrownBy { orderCommandService.processEvent(UUID.randomUUID(), REQUEST_PAY) }
            .isInstanceOf(OrderNotFoundException::class.java)
    }
}