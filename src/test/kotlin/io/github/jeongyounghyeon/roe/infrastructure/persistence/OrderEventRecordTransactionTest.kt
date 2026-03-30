package io.github.jeongyounghyeon.roe.infrastructure.persistence

import io.github.jeongyounghyeon.roe.application.order.OrderCommandService
import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import io.github.jeongyounghyeon.roe.domain.order.OrderEventRecord
import io.github.jeongyounghyeon.roe.domain.order.OrderRepository
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean

@SpringBootTest
@ActiveProfiles("test")
class OrderEventRecordTransactionTest {

    @Autowired
    lateinit var orderCommandService: OrderCommandService

    @Autowired
    lateinit var orderRepository: OrderRepository

    @MockitoSpyBean
    lateinit var orderEventRecordJpaRepository: OrderEventRecordJpaRepository

    @Test
    fun `이벤트 처리 성공 시 Order와 OrderEventRecord가 함께 저장된다`() {
        val order = orderRepository.save(Order.create())

        orderCommandService.processEvent(order.id, OrderEvent.REQUEST_PAY)

        val savedOrder = orderRepository.findById(order.id)!!
        val records = orderEventRecordJpaRepository.findByOrderId(order.id)

        assertThat(savedOrder.status).isEqualTo(OrderStatus.PAYMENT_PROCESSING)
        assertThat(records).hasSize(1)
        assertThat(records[0].event).isEqualTo(OrderEvent.REQUEST_PAY.name)
        assertThat(records[0].status).isEqualTo(OrderStatus.PAYMENT_PROCESSING.name)
        assertThat(records[0].publishedAt).isNull()
    }

    @Test
    fun `OrderEventRecord 저장 실패 시 Order 변경도 롤백된다`() {
        val order = orderRepository.save(Order.create())
        doThrow(RuntimeException("저장 실패"))
            .`when`(orderEventRecordJpaRepository)
            .save(any(OrderEventRecord::class.java))

        assertThatThrownBy { orderCommandService.processEvent(order.id, OrderEvent.REQUEST_PAY) }
            .isInstanceOf(RuntimeException::class.java)

        val savedOrder = orderRepository.findById(order.id)!!
        assertThat(savedOrder.status).isEqualTo(OrderStatus.PENDING_PAYMENT)
    }
}