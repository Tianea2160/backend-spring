package com.teamteam.backend.domain.member.dto

import com.teamteam.backend.domain.member.entity.Member
import com.teamteam.backend.shared.security.User


class MemberReadDTO(
    val id: String,
    val username: String,
) {
    companion object {
        fun from(user: User) = MemberReadDTO(
            id = user.id,
            username = user.username,
        )

        fun from(member: Member) = MemberReadDTO(
            id = member.id,
            username = member.username,
        )
    }
}