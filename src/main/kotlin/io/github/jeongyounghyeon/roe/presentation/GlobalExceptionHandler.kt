package io.github.jeongyounghyeon.roe.presentation

import io.github.jeongyounghyeon.roe.application.order.exception.*
import io.github.jeongyounghyeon.roe.domain.order.exception.InvalidOrderStateTransitionException
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.web.bind.annotation.*

@RestControllerAdvice
class GlobalExceptionHandler(
    private val meterRegistry: MeterRegistry,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(OrderNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleOrderNotFound(e: OrderNotFoundException): Map<String, String?> {
        log.warn("주문을 찾을 수 없음: ${e.message}")
        return mapOf("message" to e.message)
    }

    @ExceptionHandler(InvalidOrderStateTransitionException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleInvalidTransition(e: InvalidOrderStateTransitionException): Map<String, String?> {
        log.warn("유효하지 않은 주문 상태 전이: ${e.message}")
        meterRegistry.counter("order.event.failed", "reason", "invalid_transition").increment()
        return mapOf("message" to e.message)
    }

    @ExceptionHandler(LockAcquisitionException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleLockAcquisitionFailure(e: LockAcquisitionException): Map<String, String?> {
        log.warn("분산 락 획득 실패: ${e.message}")
        meterRegistry.counter("order.event.failed", "reason", "lock_failure").increment()
        return mapOf("message" to e.message)
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleOptimisticLockingFailure(e: ObjectOptimisticLockingFailureException): Map<String, String?> {
        log.warn("낙관적 잠금 충돌 발생: ${e.message}")
        meterRegistry.counter("order.event.failed", "reason", "optimistic_lock").increment()
        return mapOf("message" to "동시 요청으로 인한 충돌이 발생했습니다. 다시 시도해 주세요.")
    }
}
