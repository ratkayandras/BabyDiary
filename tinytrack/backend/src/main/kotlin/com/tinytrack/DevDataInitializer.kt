package com.tinytrack

import com.tinytrack.domain.entity.Language
import com.tinytrack.domain.entity.User
import com.tinytrack.domain.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DevDataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val devEmail = "dev@tinytrack.app"
        if (!userRepository.existsByEmail(devEmail)) {
            userRepository.save(
                User(
                    email = devEmail,
                    passwordHash = passwordEncoder.encode("dev123"),
                    name = "Dev User",
                    preferredLanguage = Language.EN
                )
            )
            println("==============================================")
            println("  Dev account created:")
            println("  Email:    $devEmail")
            println("  Password: dev123")
            println("==============================================")
        }
    }
}
