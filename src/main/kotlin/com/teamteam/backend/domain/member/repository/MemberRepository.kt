package com.teamteam.backend.domain.member.repository

import com.teamteam.backend.domain.member.entity.Member
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository  : MongoRepository<Member, String>{
    fun findByPID(pid : String) : Member?
}