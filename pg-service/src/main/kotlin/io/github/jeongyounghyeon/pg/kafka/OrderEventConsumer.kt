package io.github.jeongyounghyeon.pg.kafka

import io.github.jeongyounghyeon.pg.client.OrderServiceClient
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(
    private val orderServiceClient: OrderServiceClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["order.status.changed"])
    fun consume(event: OrderStatusChangedEvent) {
        if (event.status != "PAYMENT_PROCESSING") return

        log.info("결제 처리 시작: orderId={}", event.orderId)
        orderServiceClient.processPayment(event.orderId)
    }
}
