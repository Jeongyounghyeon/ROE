package io.github.jeongyounghyeon.roe.application.order

import io.github.jeongyounghyeon.roe.application.lock.DistributedLockManager
import io.github.jeongyounghyeon.roe.application.order.exception.LockAcquisitionException
import io.github.jeongyounghyeon.roe.application.order.exception.OrderNotFoundException
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent.REQUEST_PAY
import io.github.jeongyounghyeon.roe.domain.order.OrderRepository
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.statemachine.config.StateMachineFactory
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class OrderCommandServiceTest {

    @Mock lateinit var orderRepository: OrderRepository
    @Mock lateinit var stateMachineFactory: StateMachineFactory<OrderStatus, OrderEvent>

    private var capturedLockKey: String? = null

    private val executingLockManager = object : DistributedLockManager {
        override fun <T> withLock(key: String, block: () -> T): T {
            capturedLockKey = key
            return block()
        }
    }

    private val failingLockManager = object : DistributedLockManager {
        override fun <T> withLock(key: String, block: () -> T): T =
            throw LockAcquisitionException(key)
    }

    @Test
    fun `processEvent 호출 시 올바른 키로 분산 락을 획득한다`() {
        val orderId = UUID.randomUUID()
        val service = OrderCommandService(orderRepository, stateMachineFactory, executingLockManager)
        `when`(orderRepository.findById(orderId)).thenReturn(null)

        assertThatThrownBy { service.processEvent(orderId, REQUEST_PAY) }
            .isInstanceOf(OrderNotFoundException::class.java)

        assertThat(capturedLockKey).isEqualTo("order:event:lock:$orderId")
    }

    @Test
    fun `락 획득 실패 시 LockAcquisitionException 이 전파된다`() {
        val orderId = UUID.randomUUID()
        val service = OrderCommandService(orderRepository, stateMachineFactory, failingLockManager)

        assertThatThrownBy { service.processEvent(orderId, REQUEST_PAY) }
            .isInstanceOf(LockAcquisitionException::class.java)
    }

    @Test
    fun `존재하지 않는 주문 처리 시 OrderNotFoundException 이 발생한다`() {
        val orderId = UUID.randomUUID()
        val service = OrderCommandService(orderRepository, stateMachineFactory, executingLockManager)
        `when`(orderRepository.findById(orderId)).thenReturn(null)

        assertThatThrownBy { service.processEvent(orderId, REQUEST_PAY) }
            .isInstanceOf(OrderNotFoundException::class.java)
    }
}