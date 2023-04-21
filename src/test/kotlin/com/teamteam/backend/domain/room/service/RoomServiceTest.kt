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
import com.teamteam.backend.domain.room.error.RoomNotFoundException
import com.teamteam.backend.domain.room.repository.RoomRepository
import com.teamteam.backend.shared.security.User
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
    given("room service에서 방을 찾을 때") {
        `when`("전체 강의실을 조회하면") {
            every { roomRepository.findAll() } returns listOf(
                Room(
                    id = "test id",
                    buildingId = "test buildingId",
                    name = "test name",
                    description = "test description",
                    capacity = 1234
                )
            )
            every { equipmentRepository.findAllByRoomId(any()) } returns emptyList()

            then("전체 강의실 리스트를 조회한다.") {
                val reads = roomService.findAll()
                reads.size shouldBe 1
                reads[0].id shouldBe "test id"
                reads[0].name shouldBe "test name"
                reads[0].description shouldBe "test description"
                reads[0].capacity shouldBe 1234
                reads[0].equipments.size shouldBe 0
                verify(exactly = 1) { roomRepository.findAll() }
            }
        }

        `when`("찾던 방이 있으면") {
            val roomId = "test roomId"
            val room = Room(
                id = roomId,
                buildingId = "test buildingId",
                name = "test name",
                description = "test description",
                capacity = 1234
            )
            every { roomRepository.findById(roomId) } returns Optional.of(room)
            every { equipmentRepository.findAllByRoomId(roomId) } returns emptyList()
            val read = roomService.findById(roomId)

            then("방 정보를 반환한다.") {
                read.id shouldBe room.id
                read.name shouldBe room.name
                read.description shouldBe room.description
                read.capacity shouldBe room.capacity
                read.equipments.size shouldBe 0
            }
        }

        `when`("찾던 방이 없으면") {
            val roomId = "test roomId"
            every { roomRepository.findById(roomId) } returns Optional.empty()

            then("room not found exceotion을 던진다.") {
                shouldThrow<RoomNotFoundException> {
                    roomService.findById(roomId)
                }
            }
        }
    }

    given("room id가 있을 때") {
        `when`("roomService.isExist(roomId)를 호출하면") {
            val r1 = "r1"
            every { roomRepository.existsById(r1) } returns true
            then("방이 있으면 true 반환") {
                roomService.isExist(r1) shouldBe true
            }
            val r2 = "r2"
            every { roomRepository.existsById(r2) } returns false
            then("방이 없으면 false 반환") {
                roomService.isExist(r2) shouldBe false
            }
        }
    }

    given("room id와 admin id가 있을 때") {
        val roomId = "roomId"
        val adminId = "adminId"

        `when`("roomService.isValid(roomId, adminId)를 호출하면") {
            val room = Room(
                id = roomId,
                buildingId = "buildingId",
                name = "name",
                description = "description",
                capacity = 1234
            )
            var building = Building(
                id = "buildingId",
                adminId = adminId,
                name = "name",
                location = "location",
                description = "description",
                imageUrl = "imageUrl"
            )
            every { roomRepository.findById(roomId) } returns Optional.of(room)
            every { buildingRepository.findById(room.buildingId) } returns Optional.of(building)

            then("권한이 있는 경우 true를 반환한다.") {
                roomService.isValid(roomId, adminId) shouldBe true
            }

            building = Building(
                id = "buildingId",
                adminId = "adminId2",
                name = "name",
                location = "location",
                description = "description",
                imageUrl = "imageUrl"
            )
            every { buildingRepository.findById(room.buildingId) } returns Optional.of(building)
            then("권한이 없는 경우 false를 반환한다.") {
                roomService.isValid(roomId, adminId) shouldBe false
            }
        }
    }

})