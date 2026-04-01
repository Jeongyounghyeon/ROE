package io.github.jeongyounghyeon.roe.infrastructure.lock

import io.github.jeongyounghyeon.roe.application.lock.DistributedLockManager
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("test")
class NoOpLockManager : DistributedLockManager {
    override fun <T> withLock(key: String, block: () -> T): T = block()
}