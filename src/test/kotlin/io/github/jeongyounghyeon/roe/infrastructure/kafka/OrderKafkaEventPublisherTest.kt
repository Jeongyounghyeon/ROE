package io.github.jeongyounghyeon.roe.infrastructure.kafka

import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
class OrderKafkaEventPublisherTest {

    @Mock
    lateinit var kafkaTemplate: KafkaTemplate<String, OrderStatusChangedEvent>

    private lateinit var publisher: OrderKafkaEventPublisher

    @BeforeEach
    fun setUp() {
        publisher = OrderKafkaEventPublisher(kafkaTemplate)
    }

    @Test
    fun `order_status_changed 토픽으로 발행된다`() {
        val order = Order.create()

        publisher.publish(order, OrderEvent.REQUEST_PAY)

        verify(kafkaTemplate).send(
            eq("order.status.changed"),
            eq(order.id.toString()),
            any(OrderStatusChangedEvent::class.java),
        )
    }

    @Test
    fun `페이로드에 orderId, event, status 가 포함된다`() {
        val order = Order.create()
        val payloadCaptor = ArgumentCaptor.forClass(OrderStatusChangedEvent::class.java)

        publisher.publish(order, OrderEvent.PAY_SUCCESS)

        verify(kafkaTemplate).send(
            any(String::class.java),
            any(String::class.java),
            payloadCaptor.capture(),
        )

        val payload = payloadCaptor.value
        assertThat(payload.orderId).isEqualTo(order.id)
        assertThat(payload.event).isEqualTo(OrderEvent.PAY_SUCCESS.name)
        assertThat(payload.status).isEqualTo(order.status.name)
    }

    @Test
    fun `orderId 를 메시지 키로 사용한다`() {
        val order = Order.create()
        val keyCaptor = ArgumentCaptor.forClass(String::class.java)

        publisher.publish(order, OrderEvent.REQUEST_PAY)

        verify(kafkaTemplate).send(
            any(String::class.java),
            keyCaptor.capture(),
            any(OrderStatusChangedEvent::class.java),
        )

        assertThat(keyCaptor.value).isEqualTo(order.id.toString())
    }
}