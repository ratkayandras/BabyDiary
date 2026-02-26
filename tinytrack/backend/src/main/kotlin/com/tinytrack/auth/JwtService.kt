package com.tinytrack.auth

import com.tinytrack.config.AppProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(private val appProperties: AppProperties) {

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(appProperties.jwt.secret.toByteArray())
    }

    fun generateToken(userDetails: UserDetails): String = buildToken(userDetails.username, appProperties.jwt.expiryMs)

    fun generateToken(email: String): String = buildToken(email, appProperties.jwt.expiryMs)

    private fun buildToken(subject: String, expiryMs: Long): String =
        Jwts.builder()
            .subject(subject)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiryMs))
            .signWith(signingKey)
            .compact()

    fun extractEmail(token: String): String = extractClaims(token).subject

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val email = extractEmail(token)
        return email == userDetails.username && !isTokenExpired(token)
    }

    fun isTokenExpired(token: String): Boolean =
        extractClaims(token).expiration.before(Date())

    private fun extractClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
