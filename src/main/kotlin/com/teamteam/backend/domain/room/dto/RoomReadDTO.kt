package com.teamteam.backend.domain.room.dto

import com.teamteam.backend.domain.building.dto.BuildingReadSimpleDTO
import com.teamteam.backend.domain.building.entity.Building
import com.teamteam.backend.domain.equipment.entity.Equipment
import com.teamteam.backend.domain.equipment.entity.EquipmentType
import com.teamteam.backend.domain.room.entity.Room
import com.teamteam.backend.domain.room.error.RoomNotFoundException

class RoomReadDTO(
    val id: String,
    val building: BuildingReadSimpleDTO,
    val name: String,
    val capacity: Long,
    val description: String,
    val equipments: Set<EquipmentType>
) {
    companion object {
        fun from(building: Building, room: Room, equipments: List<Equipment>): RoomReadDTO {
            return RoomReadDTO(
                id = room.id ?: throw RoomNotFoundException(),
                building = BuildingReadSimpleDTO.from(building),
                name = room.name,
                capacity = room.capacity,
                description = room.description,
                equipments = equipments.map { it.type }.toSet()
            )
        }
    }
}