package com.teamteam.backend.domain.building.service

import com.teamteam.backend.domain.building.dto.BuildingCreateDTO
import com.teamteam.backend.domain.building.dto.BuildingReadDTO
import com.teamteam.backend.domain.building.dto.BuildingUpdateDTO
import com.teamteam.backend.domain.building.error.BuildingAuthorizationException
import com.teamteam.backend.domain.building.error.BuildingNameConflictException
import com.teamteam.backend.domain.building.error.BuildingNotFoundException
import com.teamteam.backend.domain.building.repository.BuildingRepository
import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.member.error.MemberNotFoundException
import com.teamteam.backend.domain.member.repository.MemberRepository
import com.teamteam.backend.shared.security.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BuildingService(
    private val buildingRepository: BuildingRepository,
    private val memberRepository: MemberRepository,
    private val provider: IdentifierProvider
) {
    //*** read only logic ***//
    @Transactional(readOnly = true)
    fun findAll(user: User): List<BuildingReadDTO> {
        return buildingRepository.findAll().map { building ->
            val member = memberRepository.findByPID(building.adminId) ?: throw MemberNotFoundException()
            BuildingReadDTO.from(
                building,
                User(username = member.username, id = member.PID, password = "", role = "", building = "")
            )
        }
    }

    @Transactional(readOnly = true)
    fun findById(buildingId: String): BuildingReadDTO {
        val building = buildingRepository.findById(buildingId).orElseThrow { throw BuildingNotFoundException() }
        val member = memberRepository.findByPID(building.adminId) ?: throw MemberNotFoundException()
        val user = User(username = member.username, id = member.PID, password = "", role = "", building = "")
        return BuildingReadDTO.from(building, user)
    }

    //*** command logic ***//
    @Transactional
    fun create(user: User, dto: BuildingCreateDTO): BuildingReadDTO {
        // name is unique check
        if (buildingRepository.existsByName(dto.name))
            throw BuildingNameConflictException()
        val building = dto.toEntity(provider.generate(), user)
        return BuildingReadDTO.from(buildingRepository.save(building), user)
    }

    @Transactional
    fun update(user: User, buildingId: String, dto: BuildingUpdateDTO): BuildingReadDTO {
        val building = buildingRepository.findById(buildingId).orElseThrow { throw BuildingNotFoundException() }
        if (building.adminId != user.id)
            throw BuildingAuthorizationException()
        val updated = buildingRepository.save(dto.toEntity(buildingId, user))
        return BuildingReadDTO.from(updated, user)
    }

    @Transactional
    fun delete(user: User, buildingId: String) {
        val building = buildingRepository.findById(buildingId).orElseThrow { throw BuildingNotFoundException() }
        if (building.adminId != user.id)
            throw BuildingAuthorizationException()
        buildingRepository.deleteById(buildingId)
    }
}