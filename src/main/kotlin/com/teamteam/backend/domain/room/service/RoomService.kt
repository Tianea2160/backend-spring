package com.teamteam.backend.domain.room.service

import com.teamteam.backend.domain.building.error.BuildingNoPermissionException
import com.teamteam.backend.domain.building.error.BuildingNotFoundException
import com.teamteam.backend.domain.building.repository.BuildingRepository
import com.teamteam.backend.domain.equipment.entity.Equipment
import com.teamteam.backend.domain.equipment.repository.EquipmentRepository
import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.room.dto.RoomCreateDTO
import com.teamteam.backend.domain.room.dto.RoomReadDTO
import com.teamteam.backend.domain.room.dto.RoomUpdateDTO
import com.teamteam.backend.domain.room.entity.Room
import com.teamteam.backend.domain.room.error.RoomNotFoundException
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
    //*** read only domain logic ***//
    @Transactional(readOnly = true)
    fun findAll(): List<RoomReadDTO> = roomRepository.findAll().map { room ->
        val building = buildingRepository.findById(room.buildingId).orElseThrow { throw BuildingNotFoundException() }
        val equipments = equipmentRepository.findAllByRoomId(room.id)
        RoomReadDTO.from(building, room, equipments)
    }

    @Transactional(readOnly = true)
    fun findById(roomId: String): RoomReadDTO = roomRepository.findById(roomId)
        .orElseThrow { throw RoomNotFoundException() }
        .let { room ->
            val building =
                buildingRepository.findById(room.buildingId).orElseThrow { throw BuildingNotFoundException() }
            val equipments = equipmentRepository.findAllByRoomId(room.id)
            RoomReadDTO.from(building, room, equipments)
        }

    //*** command domain logic ***//
    @Transactional
    fun create(user: User, buildingId: String, dto: RoomCreateDTO): RoomReadDTO {
        val building = buildingRepository.findById(buildingId).orElseThrow { throw BuildingNotFoundException() }
        if (building.adminId != user.id) throw BuildingNoPermissionException()
        val room = dto.toEntity(provider.generate(), buildingId)
        roomRepository.save(room)
        val equipments = dto.equipments.map { type ->
            Equipment(
                id = provider.generate(),
                type = type,
                roomId = room.id
            )
        }.let { equipmentRepository.saveAll(it) }
        return RoomReadDTO.from(building, room, equipments)
    }

    @Transactional
    fun update(user: User, roomId: String, dto: RoomUpdateDTO): RoomReadDTO {
        // 권한 확인
        val room: Room = roomRepository.findById(roomId).orElseThrow { throw RoomNotFoundException() }
        val building = buildingRepository.findById(room.buildingId).orElseThrow { throw BuildingNotFoundException() }
        if (building.adminId != user.id) throw BuildingNoPermissionException()

        //기존의 기자재 정보를 삭제
        equipmentRepository.deleteAllByRoomId(roomId)

        // 새롭게 정보 업데이트 및 새롭게 기자재 추가
        room.update(name = dto.name, capacity = dto.capacity, description = dto.description)
        val equipments = dto.equipments.map { type ->
            Equipment(
                id = provider.generate(),
                type = type,
                roomId = room.id
            )
        }.let { equipmentRepository.saveAll(it) }
        return RoomReadDTO.from(building, room, equipments)
    }

    @Transactional
    fun delete(user: User, roomId: String) {
        val room = roomRepository.findById(roomId).orElseThrow { throw RoomNotFoundException() }
        val building = buildingRepository.findById(room.buildingId).orElseThrow { throw BuildingNotFoundException() }
        if (building.adminId != user.id) throw BuildingNoPermissionException()
        roomRepository.deleteById(roomId)
        equipmentRepository.deleteAllByRoomId(roomId)
    }

    fun isValid(roomId: String, userId: String): Boolean {
        val room = roomRepository.findById(roomId).orElseThrow { throw RoomNotFoundException() }
        val building = buildingRepository.findById(room.buildingId).orElseThrow { throw BuildingNotFoundException() }
        return building.adminId == userId
    }

    fun isExist(roomId: String): Boolean = roomRepository.existsById(roomId)
}