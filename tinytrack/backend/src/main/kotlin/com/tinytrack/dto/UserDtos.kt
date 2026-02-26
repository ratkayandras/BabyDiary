package com.tinytrack.dto

import com.tinytrack.domain.entity.Language
import com.tinytrack.domain.entity.User
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class UserDto(
    val id: UUID,
    val email: String,
    val name: String,
    val preferredLanguage: Language,
    val createdAt: Instant
)

fun User.toDto() = UserDto(
    id = id,
    email = email,
    name = name,
    preferredLanguage = preferredLanguage,
    createdAt = createdAt
)

data class UpdateUserRequest(
    @field:NotBlank(message = "Name is required")
    val name: String? = null,
    val preferredLanguage: Language? = null,
    val currentPassword: String? = null,
    val newPassword: String? = null
)
