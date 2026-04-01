package io.github.jeongyounghyeon.roe.domain.order

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "order_histories")
class OrderHistory(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    val event: OrderEvent?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    val fromStatus: OrderStatus?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val toStatus: OrderStatus,

    @Column(nullable = true)
    val reason: String? = null,

    val occurredAt: LocalDateTime = LocalDateTime.now(),

    @Id
    val id: UUID = UUID.randomUUID(),
)