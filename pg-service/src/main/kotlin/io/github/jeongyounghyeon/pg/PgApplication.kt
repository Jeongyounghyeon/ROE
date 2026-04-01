package io.github.jeongyounghyeon.pg

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PgApplication

fun main(args: Array<String>) {
    runApplication<PgApplication>(*args)
}
