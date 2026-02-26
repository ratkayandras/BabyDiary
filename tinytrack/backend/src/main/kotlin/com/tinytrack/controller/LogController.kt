package com.tinytrack.controller

import com.tinytrack.domain.entity.User
import com.tinytrack.dto.*
import com.tinytrack.service.LogService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/children/{childId}")
class LogController(private val logService: LogService) {

    // ─── Measurements ───────────────────────────────────────────────────────

    @GetMapping("/measurements")
    fun listMeasurements(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID
    ) = ResponseEntity.ok(logService.listMeasurements(user.id, childId))

    @PostMapping("/measurements")
    fun addMeasurement(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @Valid @RequestBody request: CreateMeasurementRequest
    ) = ResponseEntity.ok(logService.addMeasurement(user.id, childId, request))

    @DeleteMapping("/measurements/{measurementId}")
    fun deleteMeasurement(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @PathVariable measurementId: UUID
    ): ResponseEntity<Void> {
        logService.deleteMeasurement(user.id, childId, measurementId)
        return ResponseEntity.noContent().build()
    }

    // ─── Feeding Logs ────────────────────────────────────────────────────────

    @GetMapping("/feeding-logs")
    fun listFeedingLogs(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @RequestParam(defaultValue = "50") limit: Int
    ) = ResponseEntity.ok(logService.listFeedingLogs(user.id, childId, limit))

    @PostMapping("/feeding-logs")
    fun addFeedingLog(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @Valid @RequestBody request: CreateFeedingLogRequest
    ) = ResponseEntity.ok(logService.addFeedingLog(user.id, childId, request))

    @DeleteMapping("/feeding-logs/{logId}")
    fun deleteFeedingLog(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @PathVariable logId: UUID
    ): ResponseEntity<Void> {
        logService.deleteFeedingLog(user.id, childId, logId)
        return ResponseEntity.noContent().build()
    }

    // ─── Diaper Logs ─────────────────────────────────────────────────────────

    @GetMapping("/diaper-logs")
    fun listDiaperLogs(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @RequestParam(defaultValue = "50") limit: Int
    ) = ResponseEntity.ok(logService.listDiaperLogs(user.id, childId, limit))

    @PostMapping("/diaper-logs")
    fun addDiaperLog(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @Valid @RequestBody request: CreateDiaperLogRequest
    ) = ResponseEntity.ok(logService.addDiaperLog(user.id, childId, request))

    @DeleteMapping("/diaper-logs/{logId}")
    fun deleteDiaperLog(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @PathVariable logId: UUID
    ): ResponseEntity<Void> {
        logService.deleteDiaperLog(user.id, childId, logId)
        return ResponseEntity.noContent().build()
    }

    // ─── Sleep Logs ──────────────────────────────────────────────────────────

    @GetMapping("/sleep-logs")
    fun listSleepLogs(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @RequestParam(defaultValue = "50") limit: Int
    ) = ResponseEntity.ok(logService.listSleepLogs(user.id, childId, limit))

    @PostMapping("/sleep-logs")
    fun addSleepLog(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @Valid @RequestBody request: CreateSleepLogRequest
    ) = ResponseEntity.ok(logService.addSleepLog(user.id, childId, request))

    @DeleteMapping("/sleep-logs/{logId}")
    fun deleteSleepLog(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @PathVariable logId: UUID
    ): ResponseEntity<Void> {
        logService.deleteSleepLog(user.id, childId, logId)
        return ResponseEntity.noContent().build()
    }
}
