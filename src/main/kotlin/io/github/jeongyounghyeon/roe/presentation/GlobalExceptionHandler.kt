package io.github.jeongyounghyeon.roe.presentation

import io.github.jeongyounghyeon.roe.application.order.exception.LockAcquisitionException
import io.github.jeongyounghyeon.roe.application.order.exception.OrderNotFoundException
import io.github.jeongyounghyeon.roe.domain.order.exception.InvalidOrderStateTransitionException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleOrderNotFound(e: OrderNotFoundException): Map<String, String?> =
        mapOf("message" to e.message)

    @ExceptionHandler(InvalidOrderStateTransitionException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleInvalidTransition(e: InvalidOrderStateTransitionException): Map<String, String?> =
        mapOf("message" to e.message)

    @ExceptionHandler(LockAcquisitionException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleLockAcquisitionFailure(e: LockAcquisitionException): Map<String, String?> =
        mapOf("message" to e.message)
}
