package io.github.jeongyounghyeon.roe.infrastructure.kafka

import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
class OrderKafkaEventPublisherTest {

    @Mock
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    private lateinit var publisher: OrderKafkaEventPublisher

    @BeforeEach
    fun setUp() {
        publisher = OrderKafkaEventPublisher(kafkaTemplate)
    }

    @Test
    fun `상태 전이 후 order_status_changed 토픽으로 이벤트가 발행된다`() {
        val order = Order.create()

        publisher.publish(order, OrderEvent.REQUEST_PAY)

        verify(kafkaTemplate).send(
            eq("order.status.changed"),
            eq(order.id.toString()),
            any(String::class.java),
        )
    }
}