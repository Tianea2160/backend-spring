package com.teamteam.backend.domain.building.dto

import com.teamteam.backend.domain.building.entity.Building
import com.teamteam.backend.shared.security.User

class BuildingUpdateDTO(
    val name: String,
    val location: String,
    val description: String,
    val imageUrl: String
) {
    fun toEntity(buildingId: String, user: User): Building = Building(
        id = buildingId,
        name = this.name,
        adminId = user.id,
        location = this.location,
        description = this.description,
        imageUrl = imageUrl
    )
}