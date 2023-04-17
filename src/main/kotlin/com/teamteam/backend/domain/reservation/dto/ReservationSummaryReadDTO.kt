package com.teamteam.backend.domain.reservation.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.teamteam.backend.domain.member.dto.MemberReadDTO
import com.teamteam.backend.domain.reservation.entity.ReservationStatus
import com.teamteam.backend.domain.reservation.entity.ReservationSummary
import com.teamteam.backend.domain.reservation.entity.ReservationTime
import com.teamteam.backend.domain.room.dto.RoomReadDTO
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReservationSummaryReadDTO(
    val id: String,
    val activity: String,
    val status: ReservationStatus,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val room: RoomReadDTO,
    val user: MemberReadDTO,
    val times: List<ReservationTimeReadDTO>,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING, timezone = "Asia/Seoul")
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING, timezone = "Asia/Seoul")
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun from(
            memberReadDTO: MemberReadDTO,
            summary: ReservationSummary,
            roomReadDTO: RoomReadDTO,
            times: List<ReservationTime>
        ) = ReservationSummaryReadDTO(
            id = summary.id,
            activity = summary.activity,
            startDate = summary.startDate,
            endDate = summary.endDate,
            room = roomReadDTO,
            status = summary.status,
            user = memberReadDTO,
            times = times.map { t -> ReservationTimeReadDTO.from(t) },
            createdAt = summary.createdAt,
            modifiedAt = summary.modifiedAt
        )
    }
}


class ReservationTimeReadDTO(
    val id: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val dayOfWeek: DayOfWeek
) {
    companion object {
        fun from(time: ReservationTime) = ReservationTimeReadDTO(
            id = time.id,
            startTime = time.startTime,
            endTime = time.endTime,
            dayOfWeek = time.dayOfWeek
        )
    }
}