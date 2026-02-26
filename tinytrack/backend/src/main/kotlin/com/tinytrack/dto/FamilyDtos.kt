package com.tinytrack.dto

import com.tinytrack.domain.entity.Family
import com.tinytrack.domain.entity.FamilyMember
import com.tinytrack.domain.entity.FamilyRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class FamilyDto(
    val id: UUID,
    val name: String,
    val createdAt: Instant,
    val members: List<FamilyMemberDto>
)

data class FamilyMemberDto(
    val userId: UUID,
    val name: String,
    val email: String,
    val role: FamilyRole,
    val joinedAt: Instant
)

fun Family.toDto(members: List<FamilyMember>? = null) = FamilyDto(
    id = id,
    name = name,
    createdAt = createdAt,
    members = (members ?: this.members.toList()).map { it.toDto() }
)

fun FamilyMember.toDto() = FamilyMemberDto(
    userId = user.id,
    name = user.name,
    email = user.email,
    role = role,
    joinedAt = joinedAt
)

data class CreateFamilyRequest(
    @field:NotBlank(message = "Family name is required")
    val name: String
)

data class InviteMemberRequest(
    @field:Email(message = "Invalid email address")
    @field:NotBlank(message = "Email is required")
    val email: String
)
