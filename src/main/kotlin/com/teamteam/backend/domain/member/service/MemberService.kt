package com.teamteam.backend.domain.member.service

import com.teamteam.backend.domain.member.dto.MemberReadDTO
import com.teamteam.backend.domain.member.error.MemberNotFoundException
import com.teamteam.backend.domain.member.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberService(private val userRepository: MemberRepository) {
    fun findById(userId: String): MemberReadDTO =
        userRepository.findByPID(userId)?.let { member -> MemberReadDTO.from(member) } ?: throw MemberNotFoundException()
}