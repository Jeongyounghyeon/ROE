package io.github.jeongyounghyeon.roe.application.outbox

import io.github.jeongyounghyeon.roe.domain.order.OrderEventRecord

interface OrderEventRecordRepository {
    fun save(record: OrderEventRecord)
}