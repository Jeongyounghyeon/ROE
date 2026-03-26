package io.github.jeongyounghyeon.roe.infrastructure.persistence

import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderRepository
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.PENDING_PAYMENT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class OrderRepositoryImplTest {

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Test
    fun `주문 저장 후 ID로 조회할 수 있어야 한다`() {
        val order = Order.create()

        orderRepository.save(order)
        val found = orderRepository.findById(order.id)

        assertThat(found).isNotNull
        assertThat(found?.id).isEqualTo(order.id)
        assertThat(found?.status).isEqualTo(PENDING_PAYMENT)
    }

    @Test
    fun `저장된 주문은 이력을 포함해야 한다`() {
        val order = Order.create()

        orderRepository.save(order)
        val found = orderRepository.findById(order.id)

        assertThat(found?.histories).hasSize(1)
        assertThat(found?.histories?.first()?.toStatus).isEqualTo(PENDING_PAYMENT)
    }

    @Test
    fun `존재하지 않는 ID 조회 시 null 을 반환해야 한다`() {
        val result = orderRepository.findById(java.util.UUID.randomUUID())

        assertThat(result).isNull()
    }
}