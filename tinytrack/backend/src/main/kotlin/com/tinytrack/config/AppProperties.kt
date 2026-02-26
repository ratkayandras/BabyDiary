package com.tinytrack.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties,
    val mail: MailProperties,
    val baseUrl: String = "http://localhost:5173"
) {
    data class JwtProperties(
        val secret: String,
        val expiryMs: Long = 900_000L,
        val refreshExpiryMs: Long = 604_800_000L
    )

    data class MailProperties(
        val from: String = "noreply@tinytrack.app"
    )
}
