package com.tinytrack.controller

import com.tinytrack.domain.entity.User
import com.tinytrack.dto.*
import com.tinytrack.service.FamilyService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/families")
class FamilyController(private val familyService: FamilyService) {

    @GetMapping
    fun list(@AuthenticationPrincipal user: User): ResponseEntity<List<FamilyDto>> =
        ResponseEntity.ok(familyService.listFamilies(user.id))

    @PostMapping
    fun create(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody request: CreateFamilyRequest
    ): ResponseEntity<FamilyDto> =
        ResponseEntity.ok(familyService.createFamily(user.id, request))

    @GetMapping("/{familyId}")
    fun get(
        @AuthenticationPrincipal user: User,
        @PathVariable familyId: java.util.UUID
    ): ResponseEntity<FamilyDto> =
        ResponseEntity.ok(familyService.getFamily(user.id, familyId))

    @PostMapping("/{familyId}/invite")
    fun invite(
        @AuthenticationPrincipal user: User,
        @PathVariable familyId: java.util.UUID,
        @Valid @RequestBody request: InviteMemberRequest
    ): ResponseEntity<Void> {
        familyService.inviteMember(user.id, familyId, request)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/accept-invite")
    fun acceptInvite(
        @AuthenticationPrincipal user: User,
        @RequestParam token: String
    ): ResponseEntity<FamilyDto> =
        ResponseEntity.ok(familyService.acceptInvitation(user.id, token))
}
