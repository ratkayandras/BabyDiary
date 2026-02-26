package com.tinytrack.service

import com.tinytrack.domain.entity.Child
import com.tinytrack.domain.repository.ChildRepository
import com.tinytrack.domain.repository.FamilyRepository
import com.tinytrack.dto.*
import com.tinytrack.exception.AccessDeniedException
import com.tinytrack.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class ChildService(
    private val childRepository: ChildRepository,
    private val familyRepository: FamilyRepository,
    private val familyService: FamilyService
) {
    fun listChildren(userId: UUID): List<ChildDto> =
        childRepository.findAllByUserId(userId).map { it.toDto() }

    fun getChild(userId: UUID, childId: UUID): ChildDto {
        val child = findAndAuthorize(userId, childId)
        return child.toDto()
    }

    fun createChild(userId: UUID, request: CreateChildRequest): ChildDto {
        familyService.ensureMember(userId, request.familyId)
        val family = familyRepository.findById(request.familyId)
            .orElseThrow { ResourceNotFoundException("Family not found") }
        val child = childRepository.save(
            Child(
                family = family,
                name = request.name,
                dateOfBirth = request.dateOfBirth,
                gender = request.gender,
                photoUrl = request.photoUrl,
                feedingReminderHours = request.feedingReminderHours
            )
        )
        return child.toDto()
    }

    fun updateChild(userId: UUID, childId: UUID, request: UpdateChildRequest): ChildDto {
        val child = findAndAuthorize(userId, childId)
        request.name?.let { child.name = it }
        request.dateOfBirth?.let { child.dateOfBirth = it }
        request.gender?.let { child.gender = it }
        request.photoUrl?.let { child.photoUrl = it }
        request.feedingReminderHours?.let { child.feedingReminderHours = it }
        return childRepository.save(child).toDto()
    }

    fun deleteChild(userId: UUID, childId: UUID) {
        val child = findAndAuthorize(userId, childId)
        childRepository.delete(child)
    }

    fun findAndAuthorize(userId: UUID, childId: UUID): Child {
        val child = childRepository.findById(childId)
            .orElseThrow { ResourceNotFoundException("Child not found") }
        familyService.ensureMember(userId, child.family.id)
        return child
    }
}
