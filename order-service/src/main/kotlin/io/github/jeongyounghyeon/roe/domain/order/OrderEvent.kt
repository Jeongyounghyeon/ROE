package io.github.jeongyounghyeon.roe.domain.order

enum class OrderEvent {
    REQUEST_PAY,      // PENDING_PAYMENT    → PAYMENT_PROCESSING
    PAY_SUCCESS,      // PAYMENT_PROCESSING → PAID
    PAY_FAIL,         // PAYMENT_PROCESSING → FAILED
    ACCEPT_ORDER,     // PAID               → PREPARING
    DISPATCH,         // PREPARING          → SHIPPED
    DELIVER,          // SHIPPED            → DELIVERED
    CANCEL_REQUEST,   // PENDING_PAYMENT / PAID / PREPARING / DELIVERED → CANCELED / REFUNDING
    REFUND_SUCCESS,   // REFUNDING          → REFUNDED
}