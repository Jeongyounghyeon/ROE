package io.github.jeongyounghyeon.roe.infrastructure.kafka

import io.github.jeongyounghyeon.roe.application.event.OrderDomainEventPublisher
import io.github.jeongyounghyeon.roe.domain.order.Order
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@Profile("!test")
class OrderKafkaEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, OrderStatusChangedEvent>,
) : OrderDomainEventPublisher {

    override fun publish(order: Order, event: OrderEvent) {
        val payload = OrderStatusChangedEvent(
            orderId = order.id,
            event = event.name,
            status = order.status.name,
            occurredAt = LocalDateTime.now(),
        )

        kafkaTemplate.send(TOPIC, order.id.toString(), payload)
    }

    companion object {
        const val TOPIC = "order.status.changed"
    }
}
