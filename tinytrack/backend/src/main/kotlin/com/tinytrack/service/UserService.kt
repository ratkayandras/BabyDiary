package com.tinytrack.service

import com.tinytrack.domain.entity.User
import com.tinytrack.domain.repository.UserRepository
import com.tinytrack.exception.ResourceNotFoundException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }

    fun findById(id: UUID): User =
        userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User not found") }

    fun findByEmail(email: String): User =
        userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("User not found") }

    fun save(user: User): User = userRepository.save(user)
}
