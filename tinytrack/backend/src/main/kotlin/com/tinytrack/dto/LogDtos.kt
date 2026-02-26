package com.tinytrack.dto

import com.tinytrack.domain.entity.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// ─── Measurement ─────────────────────────────────────────────────────────────

data class MeasurementDto(
    val id: UUID,
    val childId: UUID,
    val type: MeasurementType,
    val value: BigDecimal,
    val unit: String,
    val recordedAt: Instant,
    val notes: String?,
    val createdAt: Instant
)

fun Measurement.toDto() = MeasurementDto(
    id = id,
    childId = child.id,
    type = type,
    value = value,
    unit = unit,
    recordedAt = recordedAt,
    notes = notes,
    createdAt = createdAt
)

data class CreateMeasurementRequest(
    @field:NotNull val type: MeasurementType,
    @field:NotNull @field:Positive val value: BigDecimal,
    @field:NotNull val recordedAt: Instant,
    val notes: String? = null
)

// ─── Feeding Log ─────────────────────────────────────────────────────────────

data class FeedingLogDto(
    val id: UUID,
    val childId: UUID,
    val type: FeedingType,
    val startTime: Instant,
    val endTime: Instant?,
    val amountMl: BigDecimal?,
    val side: BreastSide?,
    val notes: String?,
    val createdAt: Instant
)

fun FeedingLog.toDto() = FeedingLogDto(
    id = id,
    childId = child.id,
    type = type,
    startTime = startTime,
    endTime = endTime,
    amountMl = amountMl,
    side = side,
    notes = notes,
    createdAt = createdAt
)

data class CreateFeedingLogRequest(
    @field:NotNull val type: FeedingType,
    @field:NotNull val startTime: Instant,
    val endTime: Instant? = null,
    val amountMl: BigDecimal? = null,
    val side: BreastSide? = null,
    val notes: String? = null
)

// ─── Diaper Log ──────────────────────────────────────────────────────────────

data class DiaperLogDto(
    val id: UUID,
    val childId: UUID,
    val recordedAt: Instant,
    val type: DiaperType,
    val notes: String?,
    val createdAt: Instant
)

fun DiaperLog.toDto() = DiaperLogDto(
    id = id,
    childId = child.id,
    recordedAt = recordedAt,
    type = type,
    notes = notes,
    createdAt = createdAt
)

data class CreateDiaperLogRequest(
    @field:NotNull val recordedAt: Instant,
    @field:NotNull val type: DiaperType,
    val notes: String? = null
)

// ─── Sleep Log ────────────────────────────────────────────────────────────────

data class SleepLogDto(
    val id: UUID,
    val childId: UUID,
    val startTime: Instant,
    val endTime: Instant?,
    val notes: String?,
    val createdAt: Instant
)

fun SleepLog.toDto() = SleepLogDto(
    id = id,
    childId = child.id,
    startTime = startTime,
    endTime = endTime,
    notes = notes,
    createdAt = createdAt
)

data class CreateSleepLogRequest(
    @field:NotNull val startTime: Instant,
    val endTime: Instant? = null,
    val notes: String? = null
)
