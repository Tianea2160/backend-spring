package com.teamteam.backend.domain.reservation.dto

import com.teamteam.backend.domain.member.dto.MemberReadDTO
import com.teamteam.backend.domain.reservation.entity.Reservation
import java.time.LocalDateTime


class ReservationReadDTO(
    val reservationId: String,
    val user: MemberReadDTO,
    val building: String,
    val room: String,
    val activity: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    companion object {
        fun from(reservation: Reservation, member: MemberReadDTO, building: String, room: String) = ReservationReadDTO(
            reservationId = reservation.id,
            user = member,
            activity = reservation.activity,
            startTime = reservation.startTime,
            endTime = reservation.endTime,
            building = building,
            room = room
        )
    }
}