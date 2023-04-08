package com.teamteam.backend.domain.building.dto

import com.teamteam.backend.domain.building.entity.Building
import com.teamteam.backend.domain.building.error.BuildingNotFoundException

class BuildingReadSimpleDTO(
    val id: String,
    val name: String,
    val description: String
) {
    companion object {
        fun from(building: Building) = BuildingReadSimpleDTO(
            id = building.id ?: throw BuildingNotFoundException(),
            name = building.name,
            description = building.description
        )
    }
}