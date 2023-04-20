package com.teamteam.backend.domain.building.service

import com.teamteam.backend.domain.building.dto.BuildingCreateDTO
import com.teamteam.backend.domain.building.dto.BuildingUpdateDTO
import com.teamteam.backend.domain.building.entity.Building
import com.teamteam.backend.domain.building.repository.BuildingRepository
import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.member.entity.Member
import com.teamteam.backend.domain.member.repository.MemberRepository
import com.teamteam.backend.shared.security.User
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import java.util.*

class BuildingServiceTest : StringSpec({
    val buildingRepository = mockk<BuildingRepository>()
    val memberRepository = mockk<MemberRepository>()
    val provider = IdentifierProvider()
    val buildingService = BuildingService(buildingRepository, memberRepository, provider)

    val user = User(
        id = "test id",
        username = "test username",
        password = "test password",
        role = "ADMIN",
        building = "test building"
    )

    "building create test" {
        // given
        val dto = BuildingCreateDTO(
            name = "test name",
            location = "test location",
            description = "test description",
            imageUrl = "test image url"
        )

        every { buildingRepository.save(any()) } returns Building(
            id = provider.generate(),
            adminId = user.id,
            name = dto.name,
            location = dto.location,
            description = dto.description,
            imageUrl = dto.imageUrl
        )
        every { buildingRepository.existsByName(any()) } returns false

        // when
        val read = withContext(Dispatchers.IO) {
            buildingService.create(user, dto)
        }

        // then
        read.name shouldBe dto.name
        read.location shouldBe dto.location
        read.description shouldBe dto.description
        read.imageUrl shouldBe dto.imageUrl
    }

    "building update test" {
        // given
        val dto = BuildingUpdateDTO(
            name = "test name",
            location = "test location",
            description = "test description",
            imageUrl = "test image url"
        )
        val buildingId = "test building id"

        every { buildingRepository.save(any()) } returns Building(
            id = buildingId,
            adminId = user.id,
            name = dto.name,
            location = dto.location,
            description = dto.description,
            imageUrl = dto.imageUrl
        )
        every { buildingRepository.existsByName(any()) } returns false
        every { buildingRepository.findById(buildingId) } returns Optional.of(
            Building(
                id = buildingId,
                adminId = user.id,
                name = dto.name,
                location = dto.location,
                description = dto.description,
                imageUrl = dto.imageUrl
            )
        )

        // when
        val updated = withContext(Dispatchers.IO) {
            buildingService.update(
                user = user,
                buildingId = buildingId,
                dto = dto
            )
        }

        // then
        updated.name shouldBe dto.name
        updated.location shouldBe dto.location
        updated.description shouldBe dto.description
        updated.imageUrl shouldBe dto.imageUrl
    }

    "building delete test" {
        // given
        val buildingId = "test building id"
        every { buildingRepository.deleteById(buildingId) } returns Unit
        // when
        withContext(Dispatchers.IO) {
            buildingService.delete(user, buildingId)
        }
    }


    "building find all test" {
        // given
        val buildings = mutableListOf<Building>()
        for (i in 0..10) {
            buildings.add(
                Building(
                    id = provider.generate(),
                    adminId = user.id,
                    name = "test name $i",
                    location = "test location$i",
                    description = "test description$i",
                    imageUrl = "test image url$i"
                )
            )
        }

        every { buildingRepository.findAll() } returns buildings
        every { memberRepository.findByPID(any()) } returns Member(
            id = provider.generate(),
            PID = user.id,
            username = user.username,
            role = "ADMIN",
        )

        // when
        val dto = withContext(Dispatchers.IO) {
            buildingService.findAll()
        }

        // then
        assertThat(dto.size).isEqualTo(buildings.size)
        for (i in 0..10) {
            dto[i].name shouldBe buildings[i].name
            dto[i].location shouldBe buildings[i].location
            dto[i].description shouldBe buildings[i].description
            dto[i].imageUrl shouldBe buildings[i].imageUrl
        }
    }

    "building find by id test" {
        // given
        val buildingId = "test building id"
        val building = Building(
            id = buildingId,
            adminId = user.id,
            name = "test name",
            location = "test location",
            description = "test description",
            imageUrl = "test image url"
        )
        every { buildingRepository.findById(buildingId) } returns Optional.of(building)
        every { memberRepository.findByPID(any()) } returns Member(
            id = provider.generate(),
            PID = user.id,
            username = user.username,
            role = "ADMIN",
        )

        // when
        val dto = withContext(Dispatchers.IO) {
            buildingService.findById(buildingId)
        }

        // then
        dto.name shouldBe building.name
        dto.location shouldBe building.location
        dto.description shouldBe building.description
        dto.imageUrl shouldBe building.imageUrl
    }
})