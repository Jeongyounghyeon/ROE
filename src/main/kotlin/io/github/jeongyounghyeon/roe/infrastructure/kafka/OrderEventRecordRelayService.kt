package io.github.jeongyounghyeon.roe.infrastructure.kafka

import io.github.jeongyounghyeon.roe.infrastructure.persistence.OrderEventRecordJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Profile("!test")
class OrderEventRecordRelayService(
    private val orderEventRecordJpaRepository: OrderEventRecordJpaRepository,
    private val kafkaTemplate: KafkaTemplate<String, OrderStatusChangedEvent>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val TOPIC = "order.status.changed"
    }

    @Scheduled(fixedDelay = 1000)
    fun relay() {
        orderEventRecordJpaRepository.findByPublishedAtIsNull().forEach { outboxEvent ->
            try {
                log.info(outboxEvent.toString())
                kafkaTemplate.send(
                    TOPIC,
                    outboxEvent.orderId.toString(),
                    OrderStatusChangedEvent(
                        orderId = outboxEvent.orderId,
                        event = outboxEvent.event,
                        status = outboxEvent.status,
                        occurredAt = outboxEvent.createdAt,
                    ),
                )
                outboxEvent.markPublished()
                orderEventRecordJpaRepository.save(outboxEvent)
            } catch (e: Exception) {
                log.error("outbox 이벤트 발행 실패: outboxEventId=${outboxEvent.id}", e)
            }
        }
    }
}
