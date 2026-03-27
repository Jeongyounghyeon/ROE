package io.github.jeongyounghyeon.roe.infrastructure.persistence

import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import io.github.jeongyounghyeon.roe.domain.order.OrderRepository
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class OrderConcurrencyTest {

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Test
    fun `최초 저장 시 버전은 0이고 업데이트 후 증가한다`() {
        val order = orderRepository.save(Order.create())
        assertThat(order.version).isEqualTo(0)

        order.changeStatus(OrderEvent.REQUEST_PAY, OrderStatus.PAYMENT_PROCESSING)
        val updated = orderRepository.save(order)

        assertThat(updated.version).isGreaterThan(0)
    }

    @Test
    fun `같은 버전의 주문을 두 번 저장하면 두 번째는 낙관적 잠금 예외가 발생한다`() {
        val order = orderRepository.save(Order.create())

        val ref1 = orderRepository.findById(order.id)!!
        val ref2 = orderRepository.findById(order.id)!!

        // ref1 먼저 저장 → version 증가
        ref1.changeStatus(OrderEvent.REQUEST_PAY, OrderStatus.PAYMENT_PROCESSING)
        orderRepository.save(ref1)

        // ref2는 이전 version으로 저장 시도 → 예외
        ref2.changeStatus(OrderEvent.REQUEST_PAY, OrderStatus.PAYMENT_PROCESSING)
        assertThatThrownBy { orderRepository.save(ref2) }
            .isInstanceOf(ObjectOptimisticLockingFailureException::class.java)
    }
}
