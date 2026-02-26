package com.tinytrack.domain.repository

import com.tinytrack.domain.entity.*
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByToken(token: String): Optional<RefreshToken>
    fun deleteByUser(user: User)
    fun deleteByToken(token: String)
}

interface FamilyRepository : JpaRepository<Family, UUID> {
    @Query("""
        SELECT DISTINCT f FROM Family f
        JOIN f.members m
        WHERE m.user.id = :userId
    """)
    fun findAllByUserId(@Param("userId") userId: UUID): List<Family>
}

interface FamilyMemberRepository : JpaRepository<FamilyMember, FamilyMemberId> {
    fun findByFamilyIdAndUserId(familyId: UUID, userId: UUID): Optional<FamilyMember>
    fun existsByFamilyIdAndUserId(familyId: UUID, userId: UUID): Boolean
    @Query("SELECT m FROM FamilyMember m JOIN FETCH m.user WHERE m.family.id = :familyId")
    fun findAllByFamilyId(@Param("familyId") familyId: UUID): List<FamilyMember>
}

interface FamilyInvitationRepository : JpaRepository<FamilyInvitation, UUID> {
    fun findByToken(token: String): Optional<FamilyInvitation>
}

interface ChildRepository : JpaRepository<Child, UUID> {
    @Query("""
        SELECT c FROM Child c
        JOIN FamilyMember m ON m.family.id = c.family.id
        WHERE m.user.id = :userId
        ORDER BY c.name
    """)
    fun findAllByUserId(@Param("userId") userId: UUID): List<Child>

    @Query("""
        SELECT c FROM Child c
        WHERE c.family.id = :familyId
        ORDER BY c.name
    """)
    fun findAllByFamilyId(@Param("familyId") familyId: UUID): List<Child>
}

interface MeasurementRepository : JpaRepository<Measurement, UUID> {
    fun findByChildIdOrderByRecordedAtDesc(childId: UUID): List<Measurement>
    fun findByChildIdAndTypeOrderByRecordedAtAsc(childId: UUID, type: MeasurementType): List<Measurement>
}

interface FeedingLogRepository : JpaRepository<FeedingLog, UUID> {
    fun findByChildIdOrderByStartTimeDesc(childId: UUID, pageable: Pageable): List<FeedingLog>
    fun findByChildIdOrderByStartTimeDesc(childId: UUID): List<FeedingLog>
    fun findTopByChildIdOrderByStartTimeDesc(childId: UUID): Optional<FeedingLog>
    fun findByChildIdAndStartTimeAfter(childId: UUID, after: Instant): List<FeedingLog>
}

interface DiaperLogRepository : JpaRepository<DiaperLog, UUID> {
    fun findByChildIdOrderByRecordedAtDesc(childId: UUID, pageable: Pageable): List<DiaperLog>
    fun findByChildIdOrderByRecordedAtDesc(childId: UUID): List<DiaperLog>
}

interface SleepLogRepository : JpaRepository<SleepLog, UUID> {
    fun findByChildIdOrderByStartTimeDesc(childId: UUID, pageable: Pageable): List<SleepLog>
    fun findByChildIdOrderByStartTimeDesc(childId: UUID): List<SleepLog>
}
