package io.github.jeongyounghyeon.roe.infrastructure.lock

import io.github.jeongyounghyeon.roe.application.lock.DistributedLockManager
import io.github.jeongyounghyeon.roe.application.order.exception.LockAcquisitionException
import org.redisson.api.RedissonClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Profile("!test")
class RedissonLockManager(private val redissonClient: RedissonClient) : DistributedLockManager {

    override fun <T> withLock(key: String, block: () -> T): T {
        val lock = redissonClient.getLock(key)
        if (!lock.tryLock(3, 5, TimeUnit.SECONDS)) {
            throw LockAcquisitionException(key)
        }
        try {
            return block()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }
}