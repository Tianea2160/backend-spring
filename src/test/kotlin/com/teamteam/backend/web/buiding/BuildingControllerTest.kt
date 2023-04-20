package com.teamteam.backend.web.buiding

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import com.teamteam.backend.domain.building.dto.BuildingCreateDTO
import com.teamteam.backend.domain.building.dto.BuildingReadDTO
import com.teamteam.backend.domain.building.dto.BuildingUpdateDTO
import com.teamteam.backend.domain.building.dto.ManagerReadDTO
import com.teamteam.backend.domain.building.service.BuildingService
import com.teamteam.backend.web.TokenProvider
import io.kotest.assertions.any
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*


@SpringBootTest
@AutoConfigureMockMvc
class BuildingControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    @MockkBean(relaxed = true)
    private val buildingService: BuildingService,
    @SpykBean
    private val provider: TokenProvider
) : DescribeSpec({
    describe("admin이") {
        val cookie = provider.createCookie("admin id", "admin username", "ADMIN_DEV")
        val create = BuildingCreateDTO(
            name = "name",
            location = "location",
            description = "description",
            imageUrl = "imageUrl"
        )
        context("건물 생성 요청을 보내면") {
            every { buildingService.create(any(), any()) } returns BuildingReadDTO(
                id = "id",
                name = create.name,
                location = create.location,
                description = create.description,
                imageUrl = create.imageUrl,
                manager = ManagerReadDTO(id = "manager id", username = "manager username")
            )

            val result = mockMvc.post("/api/building") {
                cookie(cookie)
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(create)
            }
                .andExpect { status { isCreated() } }
                .andReturn()

            it("200, BuildingReadDTO를 반환한다.") {
                val read = objectMapper.readValue<BuildingReadDTO>(result.response.contentAsString)
                read.name shouldBe create.name
                read.location shouldBe create.location
                read.description shouldBe create.description
                read.imageUrl shouldBe create.imageUrl
            }
        }

        context("건물 수정 요청을 보내면") {
            val update = BuildingUpdateDTO(
                name = "name",
                location = "location",
                description = "description",
                imageUrl = "imageUrl"
            )
            every { buildingService.update(any(), any(), any()) } returns BuildingReadDTO(
                id = "id",
                name = update.name,
                location = update.location,
                description = update.description,
                imageUrl = update.imageUrl,
                manager = ManagerReadDTO(id = "manager id", username = "manager username")
            )

            val action = mockMvc.put("/api/building/{id}", "buildingId") {
                cookie(cookie)
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(update)
            }

            it("200, BuildingReadDTO를 반환한다.") {
                action.andExpect { status { isOk() } }
                val result = action.andReturn()
                val read = objectMapper.readValue<BuildingReadDTO>(result.response.contentAsString)
                read.name shouldBe update.name
                read.location shouldBe update.location
                read.description shouldBe update.description
                read.imageUrl shouldBe update.imageUrl
            }
        }

        context("건물 삭제 요청을 보내면") {
            every { buildingService.delete(any(), any()) } returns Unit
            val action = mockMvc.delete("/api/building/{id}", "buildingId") {
                cookie(cookie)
            }
            it("200, CommonResponse를 반환한다.") {
                action.andExpect { status { isOk() } }
            }
        }

        context("건물 전체 조회 요청을 보내면") {
            every { buildingService.findAll() } returns listOf(
                BuildingReadDTO(
                    id = "id",
                    name = "name",
                    location = "location",
                    description = "description",
                    imageUrl = "imageUrl",
                    manager = ManagerReadDTO(id = "manager id", username = "manager username")
                )
            )
            val action = mockMvc.get("/api/building") {
                cookie(cookie)
            }
            it("200, BuildingReadDTO 리스트를 반환한다.") {

                action.andExpect { status { isOk() } }
                val result = action.andReturn()
                val read = objectMapper.readValue<List<BuildingReadDTO>>(result.response.contentAsString)
                read.size shouldBe 1
                read[0].name shouldBe "name"
                read[0].location shouldBe "location"
                read[0].description shouldBe "description"
                read[0].imageUrl shouldBe "imageUrl"
            }
        }

        context("건물 단건 조회 요청을 보내면") {
            val buildingId = "buildingId"
            every { buildingService.findById(buildingId) } returns BuildingReadDTO(
                id = "id",
                name = "name",
                location = "location",
                description = "description",
                imageUrl = "imageUrl",
                manager = ManagerReadDTO(id = "manager id", username = "manager username")
            )
            val action = mockMvc.get("/api/building/details/$buildingId") {
                cookie(cookie)
            }
            it("200, BuildingReadDTO를 반환한다.") {
                action.andExpect { status { isOk() } }
                val result = action.andReturn()
                val read = objectMapper.readValue<BuildingReadDTO>(result.response.contentAsString)
                read.name shouldBe "name"
                read.location shouldBe "location"
                read.description shouldBe "description"
                read.imageUrl shouldBe "imageUrl"
            }
        }
    }

    describe("일반 학생이") {
        val cookie = provider.createCookie("student id", "student username", "STUDENT")
        context("건물 생성 요청을 하면") {
            val create = BuildingCreateDTO(
                name = "name",
                location = "location",
                description = "description",
                imageUrl = "imageUrl"
            )
            val result = mockMvc.post("/api/building") {
                cookie(cookie)
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(create)
            }.andReturn()

            it("403, Forbidden을 반환한다.") {
                result.response.status shouldBe 403
            }
        }

        context("건물 삭제 요청을 하면") {
            val buildingId = "buildingId"
            it("403, Forbidden을 반환한다.") {
                mockMvc.delete("/api/building/$buildingId") {
                    cookie(cookie)
                }.andExpect { status { isForbidden() } }
            }
        }

        context("건물 수정 요청을 하면") {
            val update = BuildingUpdateDTO(
                name = "name",
                location = "location",
                description = "description",
                imageUrl = "imageUrl"
            )

            it("403, Forbidden을 반환한다.") {
                mockMvc.put("/api/building/{id}", "buildingId") {
                    cookie(cookie)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(update)
                }.andExpect { status { isForbidden() } }
            }
        }
    }
})