package com.teamteam.backend.domain.building.dto

import com.teamteam.backend.domain.building.entity.Building
import com.teamteam.backend.shared.security.User

class BuildingReadDTO(
    val id: String,
    val manager: ManagerReadDTO,
    val name: String,
    val location: String,
    val description: String,
    val imageUrl: String
) {

    companion object {
        fun from(building: Building, user: User) = BuildingReadDTO(
            id = building.id!!,
            manager = ManagerReadDTO(id = user.id, username = user.username),
            name = building.name,
            location = building.location,
            description = building.description,
            imageUrl = building.imageUrl
        )
    }
}

class ManagerReadDTO(
    val id: String,
    val username: String,
)