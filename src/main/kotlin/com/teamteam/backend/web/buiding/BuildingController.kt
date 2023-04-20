package com.teamteam.backend.web.buiding

import com.teamteam.backend.domain.building.dto.BuildingCreateDTO
import com.teamteam.backend.domain.building.dto.BuildingReadDTO
import com.teamteam.backend.domain.building.dto.BuildingUpdateDTO
import com.teamteam.backend.domain.building.service.BuildingService
import com.teamteam.backend.shared.dto.CommonResponse
import com.teamteam.backend.shared.security.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/building")
class BuildingController(
    private val buildingService: BuildingService
) {
    //*** read only controller ***//
    @GetMapping("")
    fun findAll(): ResponseEntity<List<BuildingReadDTO>> {
        return ResponseEntity.ok(buildingService.findAll())
    }

    @GetMapping("/details/{buildingId}")
    fun findById(@PathVariable buildingId: String) : ResponseEntity<BuildingReadDTO>{
        return ResponseEntity.ok(buildingService.findById(buildingId))
    }

    //*** command controller ***//
    @PostMapping("")
    fun create(
        @RequestBody dto: BuildingCreateDTO,
        authentication: Authentication
    ): ResponseEntity<BuildingReadDTO> {
        val user: User = authentication.principal as User
        return ResponseEntity.status(HttpStatus.CREATED).body(buildingService.create(user, dto))
    }


    @PutMapping("/{buildingId}")
    fun update(
        @RequestBody dto: BuildingUpdateDTO,
        @PathVariable buildingId: String,
        authentication: Authentication
    ): ResponseEntity<BuildingReadDTO> {
        val user: User = authentication.principal as User
        val updated = buildingService.update(user, buildingId, dto)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{buildingId}")
    fun delete(
        @PathVariable buildingId: String,
        authentication: Authentication
    ): ResponseEntity<CommonResponse> {
        val user: User = authentication.principal as User
        buildingService.delete(user, buildingId)
        val body = CommonResponse(
            "building($buildingId) delete success",
            "building_delete_success",
            200
        )
        return ResponseEntity.ok(body)
    }
}