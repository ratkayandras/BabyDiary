package com.tinytrack.controller

import com.tinytrack.domain.entity.User
import com.tinytrack.dto.UpdateUserRequest
import com.tinytrack.dto.UserDto
import com.tinytrack.dto.toDto
import com.tinytrack.exception.BadRequestException
import com.tinytrack.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
) {
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal user: User): ResponseEntity<UserDto> =
        ResponseEntity.ok(user.toDto())

    @PutMapping("/me")
    fun updateMe(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserDto> {
        request.name?.let { user.name = it }
        request.preferredLanguage?.let { user.preferredLanguage = it }

        if (request.newPassword != null) {
            if (request.currentPassword == null) {
                throw BadRequestException("Current password is required to set a new password")
            }
            if (!passwordEncoder.matches(request.currentPassword, user.password)) {
                throw BadRequestException("Current password is incorrect")
            }
            user.updatePassword(passwordEncoder.encode(request.newPassword))
        }

        return ResponseEntity.ok(userService.save(user).toDto())
    }
}
