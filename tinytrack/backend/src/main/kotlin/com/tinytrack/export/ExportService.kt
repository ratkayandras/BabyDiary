package com.tinytrack.export

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import com.opencsv.CSVWriter
import com.tinytrack.domain.entity.Child
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
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())

    fun exportPdf(child: Child): ByteArray {
        val out = ByteArrayOutputStream()
        val document = Document(PageSize.A4, 40f, 40f, 60f, 40f)
        PdfWriter.getInstance(document, out)
        document.open()

        val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, Color.DARK_GRAY)
        val sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, Color(60, 100, 160))
        val bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10f)
        val headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, Color.WHITE)

        document.add(Paragraph("TinyTrack — Child Report", titleFont))
        document.add(Paragraph("Child: ${child.name}", bodyFont))
        document.add(Paragraph("Date of Birth: ${child.dateOfBirth}", bodyFont))
        document.add(Paragraph("Gender: ${child.gender}", bodyFont))
        document.add(Paragraph("Generated: ${dtf.format(java.time.Instant.now())}", bodyFont))
        document.add(Chunk.NEWLINE)

        // Measurements
        val measurements = measurementRepository.findByChildIdOrderByRecordedAtDesc(child.id)
        if (measurements.isNotEmpty()) {
            document.add(Paragraph("Measurements", sectionFont))
            document.add(Chunk.NEWLINE)
            val table = PdfPTable(floatArrayOf(2f, 1.5f, 1f, 2f, 3f))
            table.widthPercentage = 100f
            listOf("Date", "Type", "Value", "Unit", "Notes").forEach { h ->
                table.addCell(PdfPCell(Phrase(h, headerFont)).apply {
                    backgroundColor = Color(60, 100, 160)
                    padding = 6f
                })
            }
            measurements.forEach { m ->
                table.addCell(PdfPCell(Phrase(dtf.format(m.recordedAt), bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(m.type.name, bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(m.value.toPlainString(), bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(m.unit, bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(m.notes ?: "", bodyFont)).apply { padding = 4f })
            }
            document.add(table)
            document.add(Chunk.NEWLINE)
        }

        // Feeding logs
        val feedings = feedingLogRepository.findByChildIdOrderByStartTimeDesc(child.id)
        if (feedings.isNotEmpty()) {
            document.add(Paragraph("Feeding Logs", sectionFont))
            document.add(Chunk.NEWLINE)
            val table = PdfPTable(floatArrayOf(2f, 1.5f, 2f, 1.5f, 1.5f, 2f))
            table.widthPercentage = 100f
            listOf("Start", "Type", "End", "Amount (ml)", "Side", "Notes").forEach { h ->
                table.addCell(PdfPCell(Phrase(h, headerFont)).apply { backgroundColor = Color(60, 100, 160); padding = 6f })
            }
            feedings.take(100).forEach { f ->
                table.addCell(PdfPCell(Phrase(dtf.format(f.startTime), bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(f.type.name, bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(f.endTime?.let { dtf.format(it) } ?: "-", bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(f.amountMl?.toPlainString() ?: "-", bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(f.side?.name ?: "-", bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(f.notes ?: "", bodyFont)).apply { padding = 4f })
            }
            document.add(table)
            document.add(Chunk.NEWLINE)
        }

        // Diaper logs
        val diapers = diaperLogRepository.findByChildIdOrderByRecordedAtDesc(child.id)
        if (diapers.isNotEmpty()) {
            document.add(Paragraph("Diaper Logs", sectionFont))
            document.add(Chunk.NEWLINE)
            val table = PdfPTable(floatArrayOf(2.5f, 1.5f, 4f))
            table.widthPercentage = 100f
            listOf("Recorded At", "Type", "Notes").forEach { h ->
                table.addCell(PdfPCell(Phrase(h, headerFont)).apply { backgroundColor = Color(60, 100, 160); padding = 6f })
            }
            diapers.take(100).forEach { d ->
                table.addCell(PdfPCell(Phrase(dtf.format(d.recordedAt), bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(d.type.name, bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(d.notes ?: "", bodyFont)).apply { padding = 4f })
            }
            document.add(table)
            document.add(Chunk.NEWLINE)
        }

        // Sleep logs
        val sleeps = sleepLogRepository.findByChildIdOrderByStartTimeDesc(child.id)
        if (sleeps.isNotEmpty()) {
            document.add(Paragraph("Sleep Logs", sectionFont))
            document.add(Chunk.NEWLINE)
            val table = PdfPTable(floatArrayOf(2.5f, 2.5f, 4f))
            table.widthPercentage = 100f
            listOf("Start", "End", "Notes").forEach { h ->
                table.addCell(PdfPCell(Phrase(h, headerFont)).apply { backgroundColor = Color(60, 100, 160); padding = 6f })
            }
            sleeps.take(100).forEach { s ->
                table.addCell(PdfPCell(Phrase(dtf.format(s.startTime), bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(s.endTime?.let { dtf.format(it) } ?: "-", bodyFont)).apply { padding = 4f })
                table.addCell(PdfPCell(Phrase(s.notes ?: "", bodyFont)).apply { padding = 4f })
            }
            document.add(table)
        }

        document.close()
        return out.toByteArray()
    }

    fun exportCsv(child: Child): ByteArray {
        val sw = StringWriter()
        val writer = CSVWriter(sw)

        writer.writeNext(arrayOf("# TinyTrack Export — ${child.name}"))
        writer.writeNext(arrayOf(""))

        // Measurements
        writer.writeNext(arrayOf("## MEASUREMENTS"))
        writer.writeNext(arrayOf("id", "type", "value", "unit", "recordedAt", "notes"))
        measurementRepository.findByChildIdOrderByRecordedAtDesc(child.id).forEach { m ->
            writer.writeNext(arrayOf(m.id.toString(), m.type.name, m.value.toPlainString(), m.unit, m.recordedAt.toString(), m.notes ?: ""))
        }
        writer.writeNext(arrayOf(""))

        // Feeding
        writer.writeNext(arrayOf("## FEEDING LOGS"))
        writer.writeNext(arrayOf("id", "type", "startTime", "endTime", "amountMl", "side", "notes"))
        feedingLogRepository.findByChildIdOrderByStartTimeDesc(child.id).forEach { f ->
            writer.writeNext(arrayOf(f.id.toString(), f.type.name, f.startTime.toString(), f.endTime?.toString() ?: "", f.amountMl?.toPlainString() ?: "", f.side?.name ?: "", f.notes ?: ""))
        }
        writer.writeNext(arrayOf(""))

        // Diapers
        writer.writeNext(arrayOf("## DIAPER LOGS"))
        writer.writeNext(arrayOf("id", "type", "recordedAt", "notes"))
        diaperLogRepository.findByChildIdOrderByRecordedAtDesc(child.id).forEach { d ->
            writer.writeNext(arrayOf(d.id.toString(), d.type.name, d.recordedAt.toString(), d.notes ?: ""))
        }
        writer.writeNext(arrayOf(""))

        // Sleep
        writer.writeNext(arrayOf("## SLEEP LOGS"))
        writer.writeNext(arrayOf("id", "startTime", "endTime", "notes"))
        sleepLogRepository.findByChildIdOrderByStartTimeDesc(child.id).forEach { s ->
            writer.writeNext(arrayOf(s.id.toString(), s.startTime.toString(), s.endTime?.toString() ?: "", s.notes ?: ""))
        }

        writer.close()
        return sw.toString().toByteArray(Charsets.UTF_8)
    }
}
