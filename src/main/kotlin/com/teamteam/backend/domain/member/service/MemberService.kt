package com.teamteam.backend.domain.member.service

import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.member.dto.MemberReadDTO
import com.teamteam.backend.domain.member.entity.MockMember
import com.teamteam.backend.domain.member.error.MemberNotFoundException
import com.teamteam.backend.domain.member.repository.MemberRepository
import com.teamteam.backend.domain.member.repository.MockMemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val userRepository: MemberRepository,
    private val mockMemberRepository: MockMemberRepository,
    private val provider: IdentifierProvider
) {
    fun findById(userId: String, isCreatedByAdmin: Boolean = false): MemberReadDTO = if (isCreatedByAdmin) {
        mockMemberRepository.findById(userId)
            .map { member -> MemberReadDTO(id = member.id, username = member.username) }
            .orElseThrow { throw MemberNotFoundException() }
    } else {
        userRepository.findById(userId)
            .map { member -> MemberReadDTO(id = member.id, username = member.username) }
            .orElseThrow { throw MemberNotFoundException() }
    }

    @Transactional
    fun createMockMember(username: String): MemberReadDTO {
        val mock = mockMemberRepository.save(MockMember(id = provider.generate(), username = username))
        return MemberReadDTO(id = mock.id, username = mock.username)
    }

    @Transactional
    fun deleteMockMemberById(userId: String) {
        mockMemberRepository.deleteById(userId)
    }
}