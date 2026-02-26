package com.tinytrack.scheduler

import com.tinytrack.config.AppProperties
import com.tinytrack.domain.entity.Child
import com.tinytrack.domain.repository.ChildRepository
import com.tinytrack.domain.repository.FamilyMemberRepository
import com.tinytrack.domain.repository.FeedingLogRepository
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class FeedingReminderScheduler(
    private val childRepository: ChildRepository,
    private val feedingLogRepository: FeedingLogRepository,
    private val familyMemberRepository: FamilyMemberRepository,
    private val mailSender: JavaMailSender,
    private val appProperties: AppProperties
) {
    private val log = LoggerFactory.getLogger(FeedingReminderScheduler::class.java)

    // Run every 30 minutes
    @Scheduled(fixedDelay = 30 * 60 * 1000L)
    fun checkFeedingReminders() {
        val children = childRepository.findAll()
        children.forEach { child ->
            try {
                checkChild(child)
            } catch (ex: Exception) {
                log.warn("Error checking feeding reminder for child ${child.id}: ${ex.message}")
            }
        }
    }

    private fun checkChild(child: Child) {
        val thresholdHours = child.feedingReminderHours.toLong()
        val cutoff = Instant.now().minusSeconds(thresholdHours * 3600)

        val lastFeeding = feedingLogRepository.findTopByChildIdOrderByStartTimeDesc(child.id)
        val needsReminder = lastFeeding.map { it.startTime.isBefore(cutoff) }.orElse(true)

        if (needsReminder) {
            val members = familyMemberRepository.findAllByFamilyId(child.family.id)
            val emails = members.map { it.user.email }
            if (emails.isEmpty()) return

            val lastFeedingText = lastFeeding
                .map { "Last feeding was at ${it.startTime}" }
                .orElse("No feeding recorded yet")

            val message = SimpleMailMessage().apply {
                from = appProperties.mail.from
                setTo(*emails.toTypedArray())
                subject = "Feeding reminder for ${child.name}"
                text = """
                    Hi!

                    This is a reminder that ${child.name} hasn't been fed in over $thresholdHours hours.
                    $lastFeedingText.

                    Open TinyTrack to log a feeding: ${appProperties.baseUrl}

                    — TinyTrack
                """.trimIndent()
            }
            try {
                mailSender.send(message)
                log.info("Sent feeding reminder for child ${child.name} to ${emails.size} recipients")
            } catch (ex: Exception) {
                log.warn("Failed to send feeding reminder email: ${ex.message}")
            }
        }
    }
}
