package io.github.jeongyounghyeon.roe.application.lock

interface DistributedLockManager {
    fun <T> withLock(key: String, block: () -> T): T
}