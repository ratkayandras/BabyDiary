package com.tinytrack.growth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.tinytrack.domain.entity.Gender
import com.tinytrack.domain.entity.MeasurementType
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class WhoTableEntry(
    val month: Int,
    val l: Double,
    val m: Double,
    val s: Double,
    val p3: Double,
    val p15: Double,
    val p50: Double,
    val p85: Double,
    val p97: Double
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WhoDataFile(
    val boys: List<WhoTableEntry>,
    val girls: List<WhoTableEntry>
)

data class PercentileResult(
    val ageMonths: Int,
    val value: Double,
    val unit: String,
    val percentile: Double?,
    val bands: PercentileBands
)

data class PercentileBands(
    val p3: Double,
    val p15: Double,
    val p50: Double,
    val p85: Double,
    val p97: Double
)

data class GrowthAnalysisResponse(
    val childId: String,
    val childName: String,
    val weight: List<PercentileResult>,
    val height: List<PercentileResult>,
    val headCircumference: List<PercentileResult>
)

@Service
class WhoGrowthService(private val mapper: ObjectMapper) {

    // Maps: "weight_boys", "weight_girls", "height_boys", etc.
    private val tables = mutableMapOf<String, List<WhoTableEntry>>()

    @PostConstruct
    fun load() {
        val types = mapOf(
            "weight" to "who-data/weight_for_age.json",
            "height" to "who-data/height_for_age.json",
            "head" to "who-data/head_circumference_for_age.json"
        )
        types.forEach { (key, path) ->
            try {
                val resource = ClassPathResource(path)
                val data: WhoDataFile = mapper.readValue(resource.inputStream, WhoDataFile::class.java)
                tables["${key}_boys"] = data.boys
                tables["${key}_girls"] = data.girls
            } catch (ex: Exception) {
                // WHO data not found — percentiles will be unavailable
            }
        }
    }

    fun calculatePercentile(value: Double, month: Int, type: String, gender: Gender): PercentileBands? {
        val genderKey = if (gender == Gender.MALE) "boys" else "girls"
        val table = tables["${type}_$genderKey"] ?: return null
        val entry = table.minByOrNull { Math.abs(it.month - month) } ?: return null
        return PercentileBands(p3 = entry.p3, p15 = entry.p15, p50 = entry.p50, p85 = entry.p85, p97 = entry.p97)
    }

    fun computeZScore(value: Double, month: Int, type: String, gender: Gender): Double? {
        val genderKey = if (gender == Gender.MALE) "boys" else "girls"
        val table = tables["${type}_$genderKey"] ?: return null
        val entry = table.minByOrNull { Math.abs(it.month - month) } ?: return null
        // WHO LMS method
        val l = entry.l
        val m = entry.m
        val s = entry.s
        return if (l == 0.0) Math.log(value / m) / s
        else (Math.pow(value / m, l) - 1) / (l * s)
    }

    fun zScoreToPercentile(z: Double): Double {
        // Approximation using normal CDF
        val t = 1.0 / (1.0 + 0.2316419 * Math.abs(z))
        val d = 0.3989423 * Math.exp(-z * z / 2)
        val p = d * t * (0.3193815 + t * (-0.3565638 + t * (1.7814779 + t * (-1.8212560 + t * 1.3302744))))
        return if (z > 0) (1.0 - p) * 100 else p * 100
    }

    fun getTypeKey(measurementType: MeasurementType): String = when (measurementType) {
        MeasurementType.WEIGHT -> "weight"
        MeasurementType.HEIGHT -> "height"
        MeasurementType.HEAD_CIRCUMFERENCE -> "head"
    }

    fun ageInMonths(dateOfBirth: LocalDate, recordedAt: LocalDate): Int {
        val days = ChronoUnit.DAYS.between(dateOfBirth, recordedAt)
        return (days / 30.4375).toInt().coerceAtMost(60)
    }
}
