package com.teamteam.backend.web.room

import com.teamteam.backend.domain.room.dto.RoomCreateDTO
import com.teamteam.backend.domain.room.dto.RoomReadDTO
import com.teamteam.backend.domain.room.dto.RoomUpdateDTO
import com.teamteam.backend.domain.room.service.RoomService
import com.teamteam.backend.shared.dto.CommonResponse
import com.teamteam.backend.shared.security.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/room")
class RoomController(
    private val roomService: RoomService
) {
    //*** read only endpoint ***//
    @GetMapping("")
    fun findAll(): ResponseEntity<List<RoomReadDTO>> = ResponseEntity.ok(roomService.findAll())


    @GetMapping("/details/{roomId}")
    fun findById(@PathVariable roomId: String): ResponseEntity<RoomReadDTO> =
        ResponseEntity.ok(roomService.findById(roomId))

    //*** command endpoint ***//
    @PostMapping("/{buildingId}")
    fun create(
        authentication: Authentication,
        @PathVariable buildingId: String,
        @RequestBody dto: RoomCreateDTO
    ): ResponseEntity<RoomReadDTO> {
        val user = authentication.principal as User
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.create(user, buildingId, dto))
    }

    @PutMapping("/{roomId}")
    fun update(
        authentication: Authentication,
        @PathVariable roomId: String,
        @RequestBody dto: RoomUpdateDTO
    ): ResponseEntity<RoomReadDTO> {
        val user = authentication.principal as User
        return ResponseEntity.ok(roomService.update(user, roomId, dto))
    }

    @DeleteMapping("/{roomId}")
    fun delete(
        authentication: Authentication,
        @PathVariable roomId: String
    ): ResponseEntity<CommonResponse> {
        val user = authentication.principal as User
        roomService.delete(user, roomId)
        return ResponseEntity.ok(
            CommonResponse(
                message = "Room deleted successfully",
                code = "room_delete_success",
                status = 200
            )
        )
    }
}