package io.github.jeongyounghyeon.pg.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.converter.RecordMessageConverter
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import java.lang.reflect.Type

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String,
    @Value("\${spring.kafka.consumer.auto-offset-reset}") private val autoOffsetReset: String,
) {
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
        )
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), StringDeserializer())
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        return ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            setConsumerFactory(consumerFactory)
            setRecordMessageConverter(JsonRecordMessageConverter())
        }
    }
}

/**
 * String으로 수신한 Kafka 메시지를 @KafkaListener 파라미터 타입으로 변환한다.
 *
 * StringDeserializer로 수신 후 이 컨버터에서 Jackson으로 역직렬화한다.
 */
private class JsonRecordMessageConverter : RecordMessageConverter {

    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    override fun toMessage(
        record: ConsumerRecord<*, *>,
        acknowledgment: Any?,
        consumer: Any?,
        payloadType: Type?,
    ): Message<*> {
        val javaType = objectMapper.typeFactory.constructType(payloadType ?: Any::class.java)
        val payload: Any = objectMapper.readValue(record.value() as String, javaType)
        return MessageBuilder.withPayload(payload).build()
    }

    override fun fromMessage(message: Message<*>, defaultTopic: String?): ProducerRecord<*, *> =
        throw UnsupportedOperationException("pg-service는 Kafka producer를 사용하지 않습니다")
}
