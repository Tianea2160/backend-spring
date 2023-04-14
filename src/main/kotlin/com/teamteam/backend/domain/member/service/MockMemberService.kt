package com.teamteam.backend.domain.member.service

import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.member.dto.MemberReadDTO
import com.teamteam.backend.domain.member.entity.MockMember
import com.teamteam.backend.domain.member.repository.MockMemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MockMemberService(
    private val mockMemberRepository: MockMemberRepository,
    private val provider : IdentifierProvider
) {
    @Transactional
    fun createMockMember(username :String): MemberReadDTO {
        val mock = mockMemberRepository.save(MockMember(id = provider.generate(), username = username))
        return MemberReadDTO(id = mock.id, username = mock.username)
    }
}