package com.tinytrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TinyTrackApplication

fun main(args: Array<String>) {
    runApplication<TinyTrackApplication>(*args)
}
