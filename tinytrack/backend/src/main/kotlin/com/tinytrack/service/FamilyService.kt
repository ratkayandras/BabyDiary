package com.tinytrack.service

import com.tinytrack.config.AppProperties
import com.tinytrack.domain.entity.*
import com.tinytrack.domain.repository.*
import com.tinytrack.dto.*
import com.tinytrack.exception.AccessDeniedException
import com.tinytrack.exception.BadRequestException
import com.tinytrack.exception.ResourceNotFoundException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class FamilyService(
    private val familyRepository: FamilyRepository,
    private val familyMemberRepository: FamilyMemberRepository,
    private val familyInvitationRepository: FamilyInvitationRepository,
    private val userRepository: UserRepository,
    private val mailSender: JavaMailSender,
    private val appProperties: AppProperties
) {
    fun listFamilies(userId: UUID): List<FamilyDto> {
        return familyRepository.findAllByUserId(userId).map { family ->
            val members = familyMemberRepository.findAllByFamilyId(family.id)
            family.toDto(members)
        }
    }

    fun createFamily(userId: UUID, request: CreateFamilyRequest): FamilyDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val family = familyRepository.save(Family(name = request.name))
        val memberId = FamilyMemberId(familyId = family.id, userId = userId)
        familyMemberRepository.save(FamilyMember(id = memberId, family = family, user = user, role = FamilyRole.OWNER))
        return family.toDto(listOf(familyMemberRepository.findById(memberId).get()))
    }

    fun getFamily(userId: UUID, familyId: UUID): FamilyDto {
        ensureMember(userId, familyId)
        val family = familyRepository.findById(familyId)
            .orElseThrow { ResourceNotFoundException("Family not found") }
        val members = familyMemberRepository.findAllByFamilyId(familyId)
        return family.toDto(members)
    }

    fun inviteMember(userId: UUID, familyId: UUID, request: InviteMemberRequest) {
        val member = familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)
            .orElseThrow { AccessDeniedException("You are not a member of this family") }
        val family = member.family

        val token = UUID.randomUUID().toString()
        val invitation = FamilyInvitation(
            family = family,
            invitedBy = member.user,
            email = request.email,
            token = token,
            expiresAt = Instant.now().plusSeconds(7 * 24 * 3600)
        )
        familyInvitationRepository.save(invitation)

        val inviteUrl = "${appProperties.baseUrl}/invite?token=$token"
        try {
            val message = SimpleMailMessage().apply {
                from = appProperties.mail.from
                setTo(request.email)
                subject = "${member.user.name} invited you to join ${family.name} on TinyTrack"
                text = """
                    Hi!

                    ${member.user.name} has invited you to join the family "${family.name}" on TinyTrack.

                    Click the link below to accept the invitation (expires in 7 days):
                    $inviteUrl

                    If you don't have an account yet, you'll be able to create one.

                    — TinyTrack
                """.trimIndent()
            }
            mailSender.send(message)
        } catch (ex: Exception) {
            // Log but don't fail the API call if mail fails
        }
    }

    fun acceptInvitation(userId: UUID, token: String): FamilyDto {
        val invitation = familyInvitationRepository.findByToken(token)
            .orElseThrow { ResourceNotFoundException("Invitation not found or expired") }

        if (invitation.expiresAt.isBefore(Instant.now())) {
            throw BadRequestException("Invitation has expired")
        }
        if (invitation.acceptedAt != null) {
            throw BadRequestException("Invitation already accepted")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val family = invitation.family

        if (!familyMemberRepository.existsByFamilyIdAndUserId(family.id, userId)) {
            val memberId = FamilyMemberId(familyId = family.id, userId = userId)
            familyMemberRepository.save(FamilyMember(id = memberId, family = family, user = user, role = FamilyRole.MEMBER))
        }

        invitation.acceptedAt = Instant.now()
        familyInvitationRepository.save(invitation)

        val members = familyMemberRepository.findAllByFamilyId(family.id)
        return family.toDto(members)
    }

    fun ensureMember(userId: UUID, familyId: UUID) {
        if (!familyMemberRepository.existsByFamilyIdAndUserId(familyId, userId)) {
            throw AccessDeniedException("You do not have access to this family")
        }
    }

    fun ensureOwner(userId: UUID, familyId: UUID) {
        val member = familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)
            .orElseThrow { AccessDeniedException("You do not have access to this family") }
        if (member.role != FamilyRole.OWNER) {
            throw AccessDeniedException("Only the family owner can perform this action")
        }
    }
}
