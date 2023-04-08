package com.teamteam.backend.domain.room.dto

import com.teamteam.backend.domain.equipment.entity.EquipmentType

class RoomUpdateDTO(
    val name : String,
    val capacity : Long,
    val description : String,
    val equipments : Set<EquipmentType>
)