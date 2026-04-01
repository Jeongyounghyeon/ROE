package io.github.jeongyounghyeon.roe.domain.order

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "orders")
class Order private constructor(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus,

    val createdAt: LocalDateTime,

    @Id
    val id: UUID,

    @Version
    var version: Long = 0,
) {
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _histories: MutableList<OrderHistory> = mutableListOf()

    val histories: List<OrderHistory> get() = _histories.toList()

    fun changeStatus(event: OrderEvent, toStatus: OrderStatus, reason: String? = null) {
        val fromStatus = this.status
        this.status = toStatus
        record(event = event, fromStatus = fromStatus, toStatus = toStatus, reason = reason)
    }

    private fun record(event: OrderEvent?, fromStatus: OrderStatus?, toStatus: OrderStatus, reason: String? = null) {
        _histories.add(
            OrderHistory(
                order = this,
                event = event,
                fromStatus = fromStatus,
                toStatus = toStatus,
                reason = reason,
            )
        )
    }

    companion object {
        fun create(): Order {
            val order = Order(
                id = UUID.randomUUID(),
                status = OrderStatus.PENDING_PAYMENT,
                createdAt = LocalDateTime.now(),
            )
            order.record(event = null, fromStatus = null, toStatus = OrderStatus.PENDING_PAYMENT)
            return order
        }
    }
}