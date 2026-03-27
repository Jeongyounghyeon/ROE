package io.github.jeongyounghyeon.roe.application.order.exception

class LockAcquisitionException(key: String) : RuntimeException("분산 락 획득에 실패했습니다: $key")