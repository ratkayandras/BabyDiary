package com.tinytrack.service

import com.tinytrack.domain.entity.*
import com.tinytrack.domain.repository.*
import com.tinytrack.dto.*
import com.tinytrack.exception.ResourceNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class LogService(
    private val measurementRepository: MeasurementRepository,
    private val feedingLogRepository: FeedingLogRepository,
    private val diaperLogRepository: DiaperLogRepository,
    private val sleepLogRepository: SleepLogRepository,
    private val childService: ChildService
) {
    // ─── Measurements ───────────────────────────────────────────────────────

    fun listMeasurements(userId: UUID, childId: UUID): List<MeasurementDto> {
        childService.findAndAuthorize(userId, childId)
        return measurementRepository.findByChildIdOrderByRecordedAtDesc(childId).map { it.toDto() }
    }

    fun addMeasurement(userId: UUID, childId: UUID, req: CreateMeasurementRequest): MeasurementDto {
        val child = childService.findAndAuthorize(userId, childId)
        val (value, unit) = when (req.type) {
            MeasurementType.WEIGHT -> {
                // Always store weight in kg; convert from g if needed
                if (req.inputUnit == "g")
                    req.value.divide(java.math.BigDecimal(1000)) to "kg"
                else
                    req.value to "kg"
            }
            MeasurementType.HEIGHT, MeasurementType.HEAD_CIRCUMFERENCE -> req.value to "cm"
        }
        val measurement = measurementRepository.save(
            Measurement(child = child, type = req.type, value = value, unit = unit, recordedAt = req.recordedAt, notes = req.notes)
        )
        return measurement.toDto()
    }

    fun deleteMeasurement(userId: UUID, childId: UUID, measurementId: UUID) {
        childService.findAndAuthorize(userId, childId)
        val m = measurementRepository.findById(measurementId)
            .orElseThrow { ResourceNotFoundException("Measurement not found") }
        measurementRepository.delete(m)
    }

    // ─── Feeding Logs ────────────────────────────────────────────────────────

    fun listFeedingLogs(userId: UUID, childId: UUID, limit: Int = 50): List<FeedingLogDto> {
        childService.findAndAuthorize(userId, childId)
        return feedingLogRepository.findByChildIdOrderByStartTimeDesc(childId, PageRequest.of(0, limit)).map { it.toDto() }
    }

    fun addFeedingLog(userId: UUID, childId: UUID, req: CreateFeedingLogRequest): FeedingLogDto {
        val child = childService.findAndAuthorize(userId, childId)
        val log = feedingLogRepository.save(
            FeedingLog(child = child, type = req.type, startTime = req.startTime, endTime = req.endTime,
                amountMl = req.amountMl, formulaAmountMl = req.formulaAmountMl, side = req.side, notes = req.notes)
        )
        return log.toDto()
    }

    fun deleteFeedingLog(userId: UUID, childId: UUID, logId: UUID) {
        childService.findAndAuthorize(userId, childId)
        val log = feedingLogRepository.findById(logId)
            .orElseThrow { ResourceNotFoundException("Feeding log not found") }
        feedingLogRepository.delete(log)
    }

    // ─── Diaper Logs ─────────────────────────────────────────────────────────

    fun listDiaperLogs(userId: UUID, childId: UUID, limit: Int = 50): List<DiaperLogDto> {
        childService.findAndAuthorize(userId, childId)
        return diaperLogRepository.findByChildIdOrderByRecordedAtDesc(childId, PageRequest.of(0, limit)).map { it.toDto() }
    }

    fun addDiaperLog(userId: UUID, childId: UUID, req: CreateDiaperLogRequest): DiaperLogDto {
        val child = childService.findAndAuthorize(userId, childId)
        val log = diaperLogRepository.save(
            DiaperLog(child = child, recordedAt = req.recordedAt, type = req.type, notes = req.notes)
        )
        return log.toDto()
    }

    fun deleteDiaperLog(userId: UUID, childId: UUID, logId: UUID) {
        childService.findAndAuthorize(userId, childId)
        val log = diaperLogRepository.findById(logId)
            .orElseThrow { ResourceNotFoundException("Diaper log not found") }
        diaperLogRepository.delete(log)
    }

    // ─── Sleep Logs ──────────────────────────────────────────────────────────

    fun listSleepLogs(userId: UUID, childId: UUID, limit: Int = 50): List<SleepLogDto> {
        childService.findAndAuthorize(userId, childId)
        return sleepLogRepository.findByChildIdOrderByStartTimeDesc(childId, PageRequest.of(0, limit)).map { it.toDto() }
    }

    fun addSleepLog(userId: UUID, childId: UUID, req: CreateSleepLogRequest): SleepLogDto {
        val child = childService.findAndAuthorize(userId, childId)
        val log = sleepLogRepository.save(
            SleepLog(child = child, startTime = req.startTime, endTime = req.endTime, notes = req.notes)
        )
        return log.toDto()
    }

    fun deleteSleepLog(userId: UUID, childId: UUID, logId: UUID) {
        childService.findAndAuthorize(userId, childId)
        val log = sleepLogRepository.findById(logId)
            .orElseThrow { ResourceNotFoundException("Sleep log not found") }
        sleepLogRepository.delete(log)
    }
}
