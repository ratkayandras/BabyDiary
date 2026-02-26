package com.tinytrack.controller

import com.tinytrack.domain.entity.User
import com.tinytrack.dto.*
import com.tinytrack.export.ExportService
import com.tinytrack.growth.*
import com.tinytrack.domain.entity.MeasurementType
import com.tinytrack.domain.repository.MeasurementRepository
import com.tinytrack.service.ChildService
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.ZoneId
import java.util.UUID

@RestController
@RequestMapping("/api/v1/children")
class ChildController(
    private val childService: ChildService,
    private val exportService: ExportService,
    private val whoGrowthService: WhoGrowthService,
    private val measurementRepository: MeasurementRepository
) {
    @GetMapping
    fun list(@AuthenticationPrincipal user: User): ResponseEntity<List<ChildDto>> =
        ResponseEntity.ok(childService.listChildren(user.id))

    @PostMapping
    fun create(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody request: CreateChildRequest
    ): ResponseEntity<ChildDto> =
        ResponseEntity.ok(childService.createChild(user.id, request))

    @GetMapping("/{childId}")
    fun get(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID
    ): ResponseEntity<ChildDto> =
        ResponseEntity.ok(childService.getChild(user.id, childId))

    @PutMapping("/{childId}")
    fun update(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @Valid @RequestBody request: UpdateChildRequest
    ): ResponseEntity<ChildDto> =
        ResponseEntity.ok(childService.updateChild(user.id, childId, request))

    @DeleteMapping("/{childId}")
    fun delete(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID
    ): ResponseEntity<Void> {
        childService.deleteChild(user.id, childId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{childId}/growth-analysis")
    fun growthAnalysis(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID
    ): ResponseEntity<GrowthAnalysisResponse> {
        val child = childService.findAndAuthorize(user.id, childId)
        val dob = child.dateOfBirth
        val gender = child.gender

        fun analyze(type: MeasurementType): List<PercentileResult> {
            val typeKey = whoGrowthService.getTypeKey(type)
            return measurementRepository
                .findByChildIdAndTypeOrderByRecordedAtAsc(childId, type)
                .map { m ->
                    val recordDate = m.recordedAt.atZone(ZoneId.systemDefault()).toLocalDate()
                    val ageMonths = whoGrowthService.ageInMonths(dob, recordDate)
                    val bands = whoGrowthService.calculatePercentile(m.value.toDouble(), ageMonths, typeKey, gender)
                        ?: PercentileBands(0.0, 0.0, 0.0, 0.0, 0.0)
                    val z = whoGrowthService.computeZScore(m.value.toDouble(), ageMonths, typeKey, gender)
                    val percentile = z?.let { whoGrowthService.zScoreToPercentile(it) }
                    PercentileResult(
                        ageMonths = ageMonths,
                        value = m.value.toDouble(),
                        unit = m.unit,
                        percentile = percentile,
                        bands = bands
                    )
                }
        }

        return ResponseEntity.ok(
            GrowthAnalysisResponse(
                childId = childId.toString(),
                childName = child.name,
                weight = analyze(MeasurementType.WEIGHT),
                height = analyze(MeasurementType.HEIGHT),
                headCircumference = analyze(MeasurementType.HEAD_CIRCUMFERENCE)
            )
        )
    }

    @GetMapping("/{childId}/export")
    fun export(
        @AuthenticationPrincipal user: User,
        @PathVariable childId: UUID,
        @RequestParam(defaultValue = "csv") format: String
    ): ResponseEntity<ByteArray> {
        val child = childService.findAndAuthorize(user.id, childId)
        return when (format.lowercase()) {
            "pdf" -> {
                val bytes = exportService.exportPdf(child)
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${child.name}-report.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(bytes)
            }
            else -> {
                val bytes = exportService.exportCsv(child)
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${child.name}-export.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(bytes)
            }
        }
    }
}
