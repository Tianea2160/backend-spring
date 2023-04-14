package com.teamteam.backend.domain.reservation.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.teamteam.backend.domain.member.dto.MockMemberCreateDTO
import com.teamteam.backend.domain.reservation.entity.ReservationSummary
import java.time.LocalDate

class ReservationSummaryAdminCreateDTO(
    val activity: String,
    val user : MockMemberCreateDTO,
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING, timezone = "Asia/Seoul")
    val startDate: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING, timezone = "Asia/Seoul")
    val endDate: LocalDate,
    val times: List<ReservationTimeCreateDTO>
) {
    fun toEntity(roomId: String, userId: String): ReservationSummary = ReservationSummary(
        activity = activity,
        startDate = startDate,
        endDate = endDate,
        roomId = roomId,
        userId = userId
    )
}