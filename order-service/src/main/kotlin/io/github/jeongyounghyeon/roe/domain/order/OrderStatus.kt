package io.github.jeongyounghyeon.roe.domain.order

enum class OrderStatus {
    PENDING_PAYMENT,
    PAYMENT_PROCESSING,
    PAID,
    PREPARING,
    SHIPPED,
    DELIVERED,
    CANCELED,
    REFUNDING,
    REFUNDED,
    FAILED,
}