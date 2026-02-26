package com.tinytrack.service

import com.tinytrack.auth.JwtService
import com.tinytrack.config.AppProperties
import com.tinytrack.domain.entity.RefreshToken
import com.tinytrack.domain.entity.User
import com.tinytrack.domain.repository.RefreshTokenRepository
import com.tinytrack.domain.repository.UserRepository
import com.tinytrack.dto.*
import com.tinytrack.exception.BadRequestException
import com.tinytrack.exception.ConflictException
import com.tinytrack.exception.UnauthorizedException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val appProperties: AppProperties
) {
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Email already registered")
        }
        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            name = request.name,
            preferredLanguage = request.preferredLanguage
        )
        userRepository.save(user)
        return issueTokens(user)
    }

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { UnauthorizedException("Invalid credentials") }
        return issueTokens(user)
    }

    fun refresh(request: RefreshTokenRequest): TokenResponse {
        val storedToken = refreshTokenRepository.findByToken(request.refreshToken)
            .orElseThrow { UnauthorizedException("Invalid refresh token") }

        if (storedToken.expiresAt.isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken)
            throw UnauthorizedException("Refresh token expired")
        }

        val newAccessToken = jwtService.generateToken(storedToken.user)
        return TokenResponse(accessToken = newAccessToken)
    }

    fun logout(refreshToken: String) {
        refreshTokenRepository.deleteByToken(refreshToken)
    }

    private fun issueTokens(user: User): AuthResponse {
        val accessToken = jwtService.generateToken(user)
        val rawRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                token = rawRefreshToken,
                expiresAt = Instant.now().plusMillis(appProperties.jwt.refreshExpiryMs)
            )
        )
        return AuthResponse(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            user = user.toDto()
        )
    }
}
