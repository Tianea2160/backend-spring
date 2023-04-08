package com.teamteam.backend.web.room

import com.teamteam.backend.domain.room.dto.RoomCreateDTO
import com.teamteam.backend.domain.room.dto.RoomReadDTO
import com.teamteam.backend.domain.room.service.RoomService
import com.teamteam.backend.shared.security.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/room")
class RoomController(
    private val roomService: RoomService
) {
    @PostMapping("/{buildingId}")
    fun create(
        authentication: Authentication,
        @PathVariable buildingId: String,
        @RequestBody dto: RoomCreateDTO
    ): ResponseEntity<RoomReadDTO> {
        val user = authentication.principal as User
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.create(user, buildingId, dto))
    }
}