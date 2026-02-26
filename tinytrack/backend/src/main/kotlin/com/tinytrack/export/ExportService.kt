package com.tinytrack.export

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import com.opencsv.CSVWriter
import com.tinytrack.domain.entity.Child
import com.tinytrack.domain.entity.Language
import com.tinytrack.domain.entity.FeedingType
import com.tinytrack.domain.entity.BreastSide
import com.tinytrack.domain.entity.DiaperType
import com.tinytrack.domain.entity.Gender
import com.tinytrack.domain.repository.*
import org.springframework.stereotype.Service
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class ExportService(
    private val measurementRepository: MeasurementRepository,
    private val feedingLogRepository: FeedingLogRepository,
    private val diaperLogRepository: DiaperLogRepository,
    private val sleepLogRepository: SleepLogRepository
) {
    private val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    // ─── Language-aware text helpers ──────────────────────────────────────────

    private fun t(lang: Language, en: String, hu: String) = if (lang == Language.HU) hu else en

    private fun feedingTypeName(type: FeedingType, lang: Language) = when (type) {
        FeedingType.BREAST   -> t(lang, "Breast",         "Szoptatás")
        FeedingType.FORMULA  -> t(lang, "Formula",        "Tápszer")
        FeedingType.SOLID    -> t(lang, "Solid Food",     "Szilárd étel")
        FeedingType.EXPRESSED -> t(lang, "Expressed Milk", "Lefejt tej")
    }

    private fun diaperTypeName(type: DiaperType, lang: Language) = when (type) {
        DiaperType.WET   -> t(lang, "Wet",           "Nedves")
        DiaperType.DIRTY -> t(lang, "Dirty",         "Piszkos")
        DiaperType.BOTH  -> t(lang, "Wet & Dirty",   "Nedves & Piszkos")
        DiaperType.DRY   -> t(lang, "Dry",           "Száraz")
    }

    private fun sideLabel(side: BreastSide?, lang: Language) = when (side) {
        BreastSide.LEFT  -> t(lang, "Left",  "Bal")
        BreastSide.RIGHT -> t(lang, "Right", "Jobb")
        BreastSide.BOTH  -> t(lang, "Both",  "Mindkettő")
        null             -> "-"
    }

    private fun genderLabel(gender: Gender, lang: Language) = when (gender) {
        Gender.MALE   -> t(lang, "Male",   "Fiú")
        Gender.FEMALE -> t(lang, "Female", "Lány")
        Gender.OTHER  -> t(lang, "Other",  "Egyéb")
    }

    // ─── PDF helpers ──────────────────────────────────────────────────────────

    private fun headerCell(text: String, font: Font): PdfPCell =
        PdfPCell(Phrase(text, font)).also {
            it.backgroundColor = Color(60, 100, 160)
            it.setPadding(6f)
        }

    private fun bodyCell(text: String, font: Font): PdfPCell =
        PdfPCell(Phrase(text, font)).also {
            it.setPadding(4f)
        }

    // ─── PDF Export ───────────────────────────────────────────────────────────

    fun exportPdf(child: Child, language: Language = Language.EN): ByteArray {
        val lang = language
        val out = ByteArrayOutputStream()
        val document = Document(PageSize.A4, 40f, 40f, 60f, 40f)
        PdfWriter.getInstance(document, out)
        document.open()

        val titleFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, Color.DARK_GRAY)
        val sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, Color(60, 100, 160))
        val bodyFont   = FontFactory.getFont(FontFactory.HELVETICA, 10f)
        val headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, Color.WHITE)

        document.add(Paragraph(t(lang, "TinyTrack — Child Report", "TinyTrack — Gyermek Riport"), titleFont))
        document.add(Paragraph("${t(lang, "Child", "Gyermek")}: ${child.name}", bodyFont))
        document.add(Paragraph("${t(lang, "Date of Birth", "Születési dátum")}: ${child.dateOfBirth}", bodyFont))
        document.add(Paragraph("${t(lang, "Gender", "Nem")}: ${genderLabel(child.gender, lang)}", bodyFont))
        document.add(Paragraph("${t(lang, "Generated", "Létrehozva")}: ${dtf.format(java.time.Instant.now())}", bodyFont))
        document.add(Chunk.NEWLINE)

        // Measurements
        val measurements = measurementRepository.findByChildIdOrderByRecordedAtDesc(child.id)
        if (measurements.isNotEmpty()) {
            document.add(Paragraph(t(lang, "Measurements", "Mérések"), sectionFont))
            document.add(Chunk.NEWLINE)
            val table = PdfPTable(floatArrayOf(2f, 1.5f, 1f, 2f, 3f))
            table.widthPercentage = 100f
            listOf(
                t(lang, "Date",  "Dátum"),
                t(lang, "Type",  "Típus"),
                t(lang, "Value", "Érték"),
                t(lang, "Unit",  "Egység"),
                t(lang, "Notes", "Megjegyzések")
            ).forEach { h -> table.addCell(headerCell(h, headerFont)) }
            measurements.forEach { m ->
                table.addCell(bodyCell(dtf.format(m.recordedAt), bodyFont))
                table.addCell(bodyCell(m.type.name, bodyFont))
                table.addCell(bodyCell(m.value.toPlainString(), bodyFont))
                table.addCell(bodyCell(m.unit, bodyFont))
                table.addCell(bodyCell(m.notes ?: "", bodyFont))
            }
            document.add(table)
            document.add(Chunk.NEWLINE)
        }

        // Feeding logs
        val feedings = feedingLogRepository.findByChildIdOrderByStartTimeDesc(child.id)
        if (feedings.isNotEmpty()) {
            document.add(Paragraph(t(lang, "Feeding Logs", "Etetési napló"), sectionFont))
            document.add(Chunk.NEWLINE)
            val table = PdfPTable(floatArrayOf(2f, 1.5f, 2f, 1.5f, 1.5f, 1.5f, 2f))
            table.widthPercentage = 100f
            listOf(
                t(lang, "Start",        "Kezdés"),
                t(lang, "Type",         "Típus"),
                t(lang, "End",          "Befejezés"),
                t(lang, "Milk (ml)",    "Tej (ml)"),
                t(lang, "Formula (ml)", "Tápszer (ml)"),
                t(lang, "Side",         "Oldal"),
                t(lang, "Notes",        "Megjegyzések")
            ).forEach { h -> table.addCell(headerCell(h, headerFont)) }
            feedings.take(100).forEach { f ->
                table.addCell(bodyCell(dtf.format(f.startTime), bodyFont))
                table.addCell(bodyCell(feedingTypeName(f.type, lang), bodyFont))
                table.addCell(bodyCell(f.endTime?.let { dtf.format(it) } ?: "-", bodyFont))
                table.addCell(bodyCell(f.amountMl?.toPlainString() ?: "-", bodyFont))
                table.addCell(bodyCell(f.formulaAmountMl?.toPlainString() ?: "-", bodyFont))
                table.addCell(bodyCell(sideLabel(f.side, lang), bodyFont))
                table.addCell(bodyCell(f.notes ?: "", bodyFont))
            }
            document.add(table)
            document.add(Chunk.NEWLINE)
        }

        // Diaper logs
        val diapers = diaperLogRepository.findByChildIdOrderByRecordedAtDesc(child.id)
        if (diapers.isNotEmpty()) {
            document.add(Paragraph(t(lang, "Diaper Logs", "Pelenkanapló"), sectionFont))
            document.add(Chunk.NEWLINE)
            val table = PdfPTable(floatArrayOf(2.5f, 1.5f, 4f))
            table.widthPercentage = 100f
            listOf(
                t(lang, "Recorded At", "Időpont"),
                t(lang, "Type",        "Típus"),
                t(lang, "Notes",       "Megjegyzések")
            ).forEach { h -> table.addCell(headerCell(h, headerFont)) }
            diapers.take(100).forEach { d ->
                table.addCell(bodyCell(dtf.format(d.recordedAt), bodyFont))
                table.addCell(bodyCell(diaperTypeName(d.type, lang), bodyFont))
                table.addCell(bodyCell(d.notes ?: "", bodyFont))
            }
            document.add(table)
            document.add(Chunk.NEWLINE)
        }

        // Sleep logs
        val sleeps = sleepLogRepository.findByChildIdOrderByStartTimeDesc(child.id)
        if (sleeps.isNotEmpty()) {
            document.add(Paragraph(t(lang, "Sleep Logs", "Alvásnapló"), sectionFont))
            document.add(Chunk.NEWLINE)
            val table = PdfPTable(floatArrayOf(2.5f, 2.5f, 4f))
            table.widthPercentage = 100f
            listOf(
                t(lang, "Start", "Elalvás"),
                t(lang, "End",   "Ébredés"),
                t(lang, "Notes", "Megjegyzések")
            ).forEach { h -> table.addCell(headerCell(h, headerFont)) }
            sleeps.take(100).forEach { s ->
                table.addCell(bodyCell(dtf.format(s.startTime), bodyFont))
                table.addCell(bodyCell(s.endTime?.let { dtf.format(it) } ?: "-", bodyFont))
                table.addCell(bodyCell(s.notes ?: "", bodyFont))
            }
            document.add(table)
        }

        document.close()
        return out.toByteArray()
    }

    // ─── CSV Export ───────────────────────────────────────────────────────────

    fun exportCsv(child: Child): ByteArray {
        val sw = StringWriter()
        val writer = CSVWriter(sw)

        writer.writeNext(arrayOf("# TinyTrack Export — ${child.name}"))
        writer.writeNext(arrayOf(""))

        writer.writeNext(arrayOf("## MEASUREMENTS"))
        writer.writeNext(arrayOf("id", "type", "value", "unit", "recordedAt", "notes"))
        measurementRepository.findByChildIdOrderByRecordedAtDesc(child.id).forEach { m ->
            writer.writeNext(arrayOf(m.id.toString(), m.type.name, m.value.toPlainString(), m.unit, m.recordedAt.toString(), m.notes ?: ""))
        }
        writer.writeNext(arrayOf(""))

        writer.writeNext(arrayOf("## FEEDING LOGS"))
        writer.writeNext(arrayOf("id", "type", "startTime", "endTime", "amountMl", "formulaAmountMl", "side", "notes"))
        feedingLogRepository.findByChildIdOrderByStartTimeDesc(child.id).forEach { f ->
            writer.writeNext(arrayOf(
                f.id.toString(), f.type.name, f.startTime.toString(), f.endTime?.toString() ?: "",
                f.amountMl?.toPlainString() ?: "", f.formulaAmountMl?.toPlainString() ?: "",
                f.side?.name ?: "", f.notes ?: ""
            ))
        }
        writer.writeNext(arrayOf(""))

        writer.writeNext(arrayOf("## DIAPER LOGS"))
        writer.writeNext(arrayOf("id", "type", "recordedAt", "notes"))
        diaperLogRepository.findByChildIdOrderByRecordedAtDesc(child.id).forEach { d ->
            writer.writeNext(arrayOf(d.id.toString(), d.type.name, d.recordedAt.toString(), d.notes ?: ""))
        }
        writer.writeNext(arrayOf(""))

        writer.writeNext(arrayOf("## SLEEP LOGS"))
        writer.writeNext(arrayOf("id", "startTime", "endTime", "notes"))
        sleepLogRepository.findByChildIdOrderByStartTimeDesc(child.id).forEach { s ->
            writer.writeNext(arrayOf(s.id.toString(), s.startTime.toString(), s.endTime?.toString() ?: "", s.notes ?: ""))
        }

        writer.close()
        return sw.toString().toByteArray(Charsets.UTF_8)
    }
}
