package com.tinytrack.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, unique = true, length = 1024)
    val token: String,

    @Column(nullable = false)
    val expiresAt: Instant,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
