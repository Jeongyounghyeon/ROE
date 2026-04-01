package io.github.jeongyounghyeon.roe.infrastructure.kafka

import io.github.jeongyounghyeon.roe.infrastructure.persistence.OrderEventRecordJpaRepository
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
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
    meterRegistry: MeterRegistry,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val TOPIC = "order.status.changed"
    }

    init {
        Gauge.builder("order.event_record.pending") {
            orderEventRecordJpaRepository.countByPublishedAtIsNull().toDouble()
        }
            .description("Number of outbox event records not yet published to Kafka")
            .register(meterRegistry)
    }

    @Scheduled(fixedDelay = 1000)
    fun relay() {
        orderEventRecordJpaRepository.findByPublishedAtIsNull().forEach { outboxEvent ->
            try {
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
                log.debug("outbox 이벤트 발행 성공: outboxEventId=${outboxEvent.id}, orderId=${outboxEvent.orderId}")
            } catch (e: Exception) {
                log.error("outbox 이벤트 발행 실패: outboxEventId=${outboxEvent.id}, orderId=${outboxEvent.orderId}", e)
            }
        }
    }
}