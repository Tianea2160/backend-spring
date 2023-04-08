package com.teamteam.backend.domain.room.service

import com.teamteam.backend.domain.building.entity.Building
import com.teamteam.backend.domain.building.repository.BuildingRepository
import com.teamteam.backend.domain.equipment.entity.Equipment
import com.teamteam.backend.domain.equipment.entity.EquipmentType
import com.teamteam.backend.domain.equipment.repository.EquipmentRepository
import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.room.dto.RoomCreateDTO
import com.teamteam.backend.domain.room.dto.RoomUpdateDTO
import com.teamteam.backend.domain.room.entity.Room
import com.teamteam.backend.domain.room.repository.RoomRepository
import com.teamteam.backend.shared.security.User
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*

class RoomServiceTest : BehaviorSpec({
    val roomRepository = mockk<RoomRepository>()
    val buildingRepository = mockk<BuildingRepository>()
    val equipmentRepository = mockk<EquipmentRepository>()

    val roomService = RoomService(
        roomRepository = roomRepository,
        buildingRepository = buildingRepository,
        provider = IdentifierProvider(),
        equipmentRepository = equipmentRepository
    )
    val user = User(
        id = "test id",
        username = "test username",
        password = "test password",
        role = "ADMIN",
        building = "test building"
    )

    given("RoomService") {
        `when`("create") {
            val equipments = mutableSetOf(EquipmentType.BOARD_MARKER, EquipmentType.ERASER)
            val dto = RoomCreateDTO(
                name = "test",
                description = "test",
                capacity = 1234,
                equipments = equipments
            )
            val buildingId = "test buildingId"
            every { roomRepository.findById(any()) } returns Optional.of(
                Room(
                    id = "test id",
                    buildingId = buildingId,
                    name = dto.name,
                    description = dto.description,
                    capacity = dto.capacity
                )
            )

            every { buildingRepository.findById(buildingId) } returns Optional.of(
                Building(
                    id = buildingId,
                    adminId = user.id,
                    name = "test name",
                    location = "test location",
                    description = "test description",
                    imageUrl = "test image url"
                )
            )
            every { roomRepository.save(any()) } returns Room(
                id = "test id",
                buildingId = buildingId,
                name = dto.name,
                description = dto.description,
                capacity = dto.capacity
            )

            every { equipmentRepository.saveAll(any<List<Equipment>>()) } returns listOf(
                Equipment(
                    id = "test id",
                    roomId = "test roomId",
                    type = EquipmentType.BOARD_MARKER
                ),
                Equipment(
                    id = "test id",
                    roomId = "test roomId",
                    type = EquipmentType.ERASER
                )
            )

            val readDTO = roomService.create(user, buildingId, dto)
            then("return RoomReadDTO") {
                assertEquals(readDTO.name, dto.name)
                assertEquals(readDTO.description, dto.description)
                assertEquals(readDTO.capacity, dto.capacity)
                assertEquals(readDTO.equipments, dto.equipments)
            }
        }

        `when`("update") {
            val equipments = mutableSetOf(EquipmentType.BOARD_MARKER, EquipmentType.ERASER)
            val dto = RoomUpdateDTO(
                name = "test",
                description = "test",
                capacity = 1234,
                equipments = equipments
            )
            val buildingId = "test buildingId"


            every { roomRepository.findById(any()) } returns Optional.of(
                Room(
                    id = "test id",
                    buildingId = buildingId,
                    name = dto.name,
                    description = dto.description,
                    capacity = dto.capacity
                )
            )

            every { buildingRepository.findById(buildingId) } returns Optional.of(
                Building(
                    id = buildingId,
                    adminId = user.id,
                    name = "test name",
                    location = "test location",
                    description = "test description",
                    imageUrl = "test image url"
                )
            )
            every { roomRepository.save(any()) } returns Room(
                id = "test id",
                buildingId = buildingId,
                name = dto.name,
                description = dto.description,
                capacity = dto.capacity
            )

            every { equipmentRepository.deleteAllByRoomId(any()) } returns Unit


            val readDTO = roomService.update(user, buildingId, dto)

            then("return RoomReadDTO") {
                assertEquals(readDTO.name, dto.name)
                assertEquals(readDTO.description, dto.description)
                assertEquals(readDTO.capacity, dto.capacity)
                assertEquals(readDTO.equipments, dto.equipments)
            }
        }

        `when`("delete") {
            val roomId = "test roomId"

            every { roomRepository.findById(roomId) } returns Optional.of(
                Room(
                    id = roomId,
                    buildingId = "test buildingId",
                    name = "test name",
                    description = "test description",
                    capacity = 1234
                )
            )
            every { buildingRepository.findById(any()) } returns Optional.of(
                Building(
                    id = "test buildingId",
                    adminId = user.id,
                    name = "test name",
                    location = "test location",
                    description = "test description",
                    imageUrl = "test image url"
                )
            )

            every { equipmentRepository.deleteAllByRoomId(roomId) } returns Unit
            every { roomRepository.deleteById(roomId) } returns Unit
            roomService.delete(user, roomId)

            then("return Unit") {
                verify(exactly = 1) { roomRepository.deleteById(roomId) }
                verify(exactly = 1) { equipmentRepository.deleteAllByRoomId(roomId) }
            }
        }

    }
}) {

}