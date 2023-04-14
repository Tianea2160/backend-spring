package com.teamteam.backend.domain.reservation.dto

import com.teamteam.backend.domain.member.dto.MemberReadDTO
import com.teamteam.backend.domain.reservation.entity.Reservation
import java.time.LocalDateTime


class ReservationReadDTO(
    val reservationId: String,
    val user: MemberReadDTO,
    val activity: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    companion object {
        fun from(reservation: Reservation, member: MemberReadDTO) = ReservationReadDTO(
            reservationId = reservation.id,
            user = member,
            activity = reservation.activity,
            startTime = reservation.startTime,
            endTime = reservation.endTime
        )
    }
}