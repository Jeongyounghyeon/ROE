package io.github.jeongyounghyeon.pg.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.UUID
import kotlin.random.Random

@Component
class OrderServiceClient(
    @Value("\${order-service.url}") orderServiceUrl: String,
    @Value("\${pg.payment.success-rate:1.0}") private val successRate: Double,
    @Value("\${pg.payment.processing-delay-ms:500}") private val processingDelayMs: Long,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create(orderServiceUrl)

    fun processPayment(orderId: UUID) {
        Thread.sleep(processingDelayMs)

        val event = if (Random.nextDouble() < successRate) "PAY_SUCCESS" else "PAY_FAIL"
        log.info("결제 처리 완료: orderId=${orderId}, result=${event}")

        restClient.post()
            .uri("/orders/$orderId/events")
            .header("Content-Type", "application/json")
            .body(mapOf("event" to event))
            .retrieve()
            .toBodilessEntity()
    }
}
