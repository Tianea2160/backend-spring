package com.teamteam.backend.domain.room.service

import com.teamteam.backend.domain.building.error.BuildingNoPermissionException
import com.teamteam.backend.domain.building.error.BuildingNotFoundException
import com.teamteam.backend.domain.building.repository.BuildingRepository
import com.teamteam.backend.domain.equipment.entity.Equipment
import com.teamteam.backend.domain.equipment.repository.EquipmentRepository
import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.room.dto.RoomCreateDTO
import com.teamteam.backend.domain.room.dto.RoomReadDTO
import com.teamteam.backend.domain.room.error.RoomCreateException
import com.teamteam.backend.domain.room.repository.RoomRepository
import com.teamteam.backend.shared.security.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val buildingRepository: BuildingRepository,
    private val equipmentRepository: EquipmentRepository,
    private val provider: IdentifierProvider
) {
    // create
    @Transactional
    fun create(user: User, buildingId: String, dto: RoomCreateDTO): RoomReadDTO {
        val building = buildingRepository.findById(buildingId).orElseThrow { throw BuildingNotFoundException() }
        // 권한 체크
        if (building.adminId != user.id) throw BuildingNoPermissionException()

        val room = dto.toEntity(buildingId)
        // 방 저장
        room.id = provider.generate()
        roomRepository.save(room)

        // 기자재 저장
        val equipments = dto.equipments.map { type ->
            Equipment(
                id = provider.generate(),
                type = type,
                roomId = room.id ?: throw RoomCreateException()
            )
        }.let { equipmentRepository.saveAll(it) }

        return RoomReadDTO.from(building, room, equipments)
    }
}