package com.tinytrack.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class FamilyRole { OWNER, MEMBER }

@Entity
@Table(name = "families")
data class Family(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "family", cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: MutableSet<FamilyMember> = mutableSetOf()
)

@Entity
@Table(name = "family_members")
data class FamilyMember(
    @EmbeddedId
    val id: FamilyMemberId = FamilyMemberId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("familyId")
    @JoinColumn(name = "family_id")
    val family: Family,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val role: FamilyRole = FamilyRole.MEMBER,

    @Column(nullable = false, updatable = false)
    val joinedAt: Instant = Instant.now()
)

@Embeddable
data class FamilyMemberId(
    val familyId: UUID = UUID.randomUUID(),
    val userId: UUID = UUID.randomUUID()
) : java.io.Serializable

@Entity
@Table(name = "family_invitations")
data class FamilyInvitation(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    val family: Family,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false)
    val invitedBy: User,

    @Column(nullable = false)
    val email: String,

    @Column(nullable = false, unique = true)
    val token: String,

    @Column(nullable = false)
    val expiresAt: Instant,

    @Column
    var acceptedAt: Instant? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
