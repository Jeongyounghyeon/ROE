package io.github.jeongyounghyeon.roe

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RoeApplication

fun main(args: Array<String>) {
    runApplication<RoeApplication>(*args)
}
