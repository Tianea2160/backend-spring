package com.teamteam.backend.domain.equipment.repository

import com.teamteam.backend.domain.equipment.entity.Equipment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EquipmentRepository : JpaRepository<Equipment, String> {
    fun findAllByRoomId(roomId: String): List<Equipment>
    fun deleteAllByRoomId(roomId: String)
}