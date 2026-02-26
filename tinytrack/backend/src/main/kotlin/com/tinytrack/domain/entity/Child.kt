package com.tinytrack.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

enum class Gender { MALE, FEMALE, OTHER }

@Entity
@Table(name = "children")
data class Child(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    val family: Family,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var dateOfBirth: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var gender: Gender = Gender.OTHER,

    @Column
    var photoUrl: String? = null,

    @Column(nullable = false)
    var feedingReminderHours: Int = 4,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
