package com.tinytrack.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// ─── Measurement ──────────────────────────────────────────────────────────────

enum class MeasurementType { WEIGHT, HEIGHT, HEAD_CIRCUMFERENCE }

@Entity
@Table(name = "measurements")
data class Measurement(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    val child: Child,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val type: MeasurementType,

    @Column(nullable = false, precision = 8, scale = 3)
    var value: BigDecimal,

    @Column(nullable = false, length = 10)
    val unit: String,

    @Column(nullable = false)
    var recordedAt: Instant,

    @Column
    var notes: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)

// ─── Feeding Log ──────────────────────────────────────────────────────────────

enum class FeedingType { BREAST, FORMULA, SOLID, EXPRESSED }
enum class BreastSide { LEFT, RIGHT, BOTH }

@Entity
@Table(name = "feeding_logs")
data class FeedingLog(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    val child: Child,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: FeedingType,

    @Column(nullable = false)
    var startTime: Instant,

    @Column
    var endTime: Instant? = null,

    /** For BREAST/EXPRESSED: mother's milk volume. For FORMULA: formula volume (legacy). */
    @Column(precision = 6, scale = 1)
    var amountMl: BigDecimal? = null,

    /** Optional formula supplement (mainly for BREAST/EXPRESSED type combined feedings). */
    @Column(name = "formula_amount_ml", precision = 6, scale = 1)
    var formulaAmountMl: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    var side: BreastSide? = null,

    @Column
    var notes: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)

// ─── Diaper Log ───────────────────────────────────────────────────────────────

enum class DiaperType { WET, DIRTY, BOTH, DRY }

@Entity
@Table(name = "diaper_logs")
data class DiaperLog(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    val child: Child,

    @Column(nullable = false)
    var recordedAt: Instant,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var type: DiaperType,

    @Column
    var notes: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)

// ─── Sleep Log ────────────────────────────────────────────────────────────────

@Entity
@Table(name = "sleep_logs")
data class SleepLog(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    val child: Child,

    @Column(nullable = false)
    var startTime: Instant,

    @Column
    var endTime: Instant? = null,

    @Column
    var notes: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
