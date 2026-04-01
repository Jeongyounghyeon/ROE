package io.github.jeongyounghyeon.roe.application.order.exception

import java.util.UUID

class OrderNotFoundException(orderId: UUID) : NoSuchElementException("Order not found: $orderId")