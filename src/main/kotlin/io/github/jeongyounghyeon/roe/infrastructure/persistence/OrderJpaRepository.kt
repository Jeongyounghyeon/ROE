package io.github.jeongyounghyeon.roe.infrastructure.persistence

import io.github.jeongyounghyeon.roe.domain.order.Order
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface OrderJpaRepository : JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = ["_histories"])
    override fun findById(id: UUID): Optional<Order>
}
