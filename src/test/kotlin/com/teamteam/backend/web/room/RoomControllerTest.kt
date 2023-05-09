//package com.teamteam.backend.web.room
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.module.kotlin.readValue
//import com.ninjasquad.springmockk.MockkBean
//import com.ninjasquad.springmockk.SpykBean
//import com.teamteam.backend.domain.building.dto.BuildingReadSimpleDTO
//import com.teamteam.backend.domain.room.dto.RoomCreateDTO
//import com.teamteam.backend.domain.room.dto.RoomReadDTO
//import com.teamteam.backend.domain.room.dto.RoomUpdateDTO
//import com.teamteam.backend.domain.room.service.RoomService
//import com.teamteam.backend.web.TokenProvider
//import io.kotest.core.spec.style.DescribeSpec
//import io.kotest.matchers.shouldBe
//import io.mockk.every
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.http.MediaType
//import org.springframework.test.web.servlet.*
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class RoomControllerTest(
//    private val mockMvc: MockMvc,
//    @MockkBean(relaxed = true)
//    private val roomService: RoomService,
//    @SpykBean
//    private val mapper: ObjectMapper,
//    @SpykBean
//    private val provider: TokenProvider,
//) : DescribeSpec({
//
//    describe("관리자가") {
//        val adminId = "admin id"
//        val username = "admin name"
//        val cookie = provider.createCookie(adminId, username, "ADMIN_DEV")
//        context("강의실 생성 api를 호출할 때") {
//            val create = RoomCreateDTO(
//                name = "room name",
//                description = "room description",
//                capacity = 10,
//                equipments = setOf()
//            )
//
//            val buildingId = "test building id"
//
//            every { roomService.create(any(), any(), any()) } returns RoomReadDTO(
//                id = "test room id",
//                name = create.name,
//                description = create.description,
//                capacity = create.capacity,
//                building = BuildingReadSimpleDTO(
//                    id = buildingId,
//                    name = "test building name",
//                    description = "test building description",
//                ),
//                equipments = setOf()
//            )
//
//            val action = mockMvc.post("/api/room/$buildingId") {
//                contentType = MediaType.APPLICATION_JSON
//                content = mapper.writeValueAsString(create)
//                cookie(cookie)
//            }
//
//            it("201, RoomReadDTO를 반환한다.") {
//                val result = action
//                    .andDo { print() }
//                    .andExpect { status { isCreated() } }
//                    .andReturn()
//                val read = mapper.readValue<RoomReadDTO>(result.response.contentAsString)
//                read.name shouldBe create.name
//                read.description shouldBe create.description
//                read.capacity shouldBe create.capacity
//                read.building.name shouldBe "test building name"
//                read.building.description shouldBe "test building description"
//            }
//        }
//        context("강의실 수정 api를 호출하면") {
//            val roomId = "test room id"
//            val update = RoomUpdateDTO(
//                name = "room name",
//                description = "room description",
//                capacity = 10,
//                equipments = setOf()
//            )
//
//            every { roomService.update(any(), any(), any()) } returns RoomReadDTO(
//                id = roomId,
//                name = update.name,
//                description = update.description,
//                capacity = update.capacity,
//                building = BuildingReadSimpleDTO(
//                    id = "test building id",
//                    name = "test building name",
//                    description = "test building description",
//                ),
//                equipments = setOf()
//            )
//
//            val action = mockMvc.put("/api/room/$roomId") {
//                contentType = MediaType.APPLICATION_JSON
//                content = mapper.writeValueAsString(update)
//                cookie(cookie)
//            }
//            it("200, RoomReadDTO를 반환한다.") {
//                val result = action.andExpect { status { isOk() } }
//                    .andReturn()
//                val read = mapper.readValue<RoomReadDTO>(result.response.contentAsString)
//                read.name shouldBe update.name
//                read.description shouldBe update.description
//                read.capacity shouldBe update.capacity
//            }
//        }
//        context("강의실 삭제 api를 호출하면") {
//            val roomId = "roomId"
//
//            val actions = mockMvc.delete("/api/room/$roomId") {
//                cookie(cookie)
//            }
//
//            it("200, RoomReadDTO를 반환한다.") {
//                actions.andExpect {
//                    status { isOk() }
//                }
//            }
//        }
//
//        context("강의실 단건 조회를 호출하면") {
//            val roomId = "roomId"
//
//            every { roomService.findById(roomId) } returns RoomReadDTO(
//                id = roomId,
//                name = "room name",
//                description = "room description",
//                capacity = 10,
//                building = BuildingReadSimpleDTO(
//                    id = "buildingId",
//                    name = "building name",
//                    description = "building description",
//                ),
//                equipments = setOf()
//            )
//            val dsl = mockMvc.get("/api/room/details/$roomId") {
//                cookie(cookie)
//            }
//            it("200, RoomReadDTO를 반환한다.") {
//                val result = dsl.andExpect { status { isOk() } }.andReturn()
//                val read = mapper.readValue<RoomReadDTO>(result.response.contentAsString)
//                read.id shouldBe roomId
//                read.name shouldBe "room name"
//                read.description shouldBe "room description"
//                read.capacity shouldBe 10
//            }
//        }
//        context("강의실 전체 조회를 호출하면") {
//            every { roomService.findAll() } returns listOf(
//                RoomReadDTO(
//                    id = "roomId",
//                    name = "room name",
//                    description = "room description",
//                    capacity = 10,
//                    building = BuildingReadSimpleDTO(
//                        id = "buildingId",
//                        name = "building name",
//                        description = "building description",
//                    ),
//                    equipments = setOf()
//                )
//            )
//            val dsl = mockMvc.get("/api/room") {
//                cookie(cookie)
//            }
//            it("200, List<RoomReadDTO>를 반환한다.") {
//                val result = dsl.andExpect { status { isOk() } }.andReturn()
//                val read = mapper.readValue<List<RoomReadDTO>>(result.response.contentAsString)
//                read.size shouldBe 1
//                read[0].id shouldBe "roomId"
//                read[0].name shouldBe "room name"
//                read[0].description shouldBe "room description"
//                read[0].capacity shouldBe 10
//            }
//        }
//    }
//    describe("일반 사용자가") {
//        val cookie = provider.createCookie("user id", "user name", "STUDENT")
//        val buildingId = "buildingId"
//        val roomId = "roomId"
//
//        context("강의실 생성 api를 호출하면") {
//            val create = RoomCreateDTO(
//                name = "room name",
//                description = "room description",
//                capacity = 10,
//                equipments = setOf()
//            )
//            val action = mockMvc.post("/api/room/$buildingId") {
//                cookie(cookie)
//                contentType = MediaType.APPLICATION_JSON
//                content = mapper.writeValueAsString(create)
//            }
//            it("403, forbidden을 반환한다.") {
//                action.andExpect { status { isForbidden() } }
//            }
//        }
//        context("강의실 수정 api를 호출하면") {
//            val update = RoomUpdateDTO(
//                name = "room name",
//                description = "room description",
//                capacity = 10,
//                equipments = setOf()
//            )
//            val dsl = mockMvc.put("/api/room/$roomId") {
//                cookie(cookie)
//                contentType = MediaType.APPLICATION_JSON
//                content = mapper.writeValueAsString(update)
//            }
//            it("403, forbidden을 반환한다.") {
//                dsl.andExpect { status { isForbidden() } }
//            }
//        }
//        context("강의실 삭제 api를 호출하면") {
//            val dsl = mockMvc.delete("/api/room/$roomId") {
//                cookie(cookie)
//            }
//            it("403, forbidden을 반환한다.") {
//                dsl.andExpect { status { isForbidden() } }
//            }
//        }
//    }
//})