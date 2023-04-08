package com.teamteam.backend.domain.room.repository

import com.teamteam.backend.domain.room.entity.Room
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomRepository : JpaRepository<Room, String> {
}