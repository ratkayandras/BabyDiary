package com.tinytrack.dto

import com.tinytrack.domain.entity.Child
import com.tinytrack.domain.entity.Gender
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ChildDto(
    val id: UUID,
    val familyId: UUID,
    val name: String,
    val dateOfBirth: LocalDate,
    val gender: Gender,
    val photoUrl: String?,
    val feedingReminderHours: Int,
    val createdAt: Instant
)

fun Child.toDto() = ChildDto(
    id = id,
    familyId = family.id,
    name = name,
    dateOfBirth = dateOfBirth,
    gender = gender,
    photoUrl = photoUrl,
    feedingReminderHours = feedingReminderHours,
    createdAt = createdAt
)

data class CreateChildRequest(
    @field:NotNull(message = "Family ID is required")
    val familyId: UUID,

    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotNull(message = "Date of birth is required")
    val dateOfBirth: LocalDate,

    val gender: Gender = Gender.OTHER,
    val photoUrl: String? = null,
    val feedingReminderHours: Int = 4
)

data class UpdateChildRequest(
    val name: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: Gender? = null,
    val photoUrl: String? = null,
    val feedingReminderHours: Int? = null
)
