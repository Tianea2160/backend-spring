package com.teamteam.backend.domain.reservation.dto

import com.teamteam.backend.domain.reservation.entity.ReservationSummary
import java.time.LocalDate


class ReservationSummaryCreateDTO(
    val activity: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val times: List<ReservationTimeCreateDTO>
) {
    fun toEntity(id: String, roomId: String, userId: String): ReservationSummary = ReservationSummary(
        id = id,
        activity = activity,
        startDate = startDate,
        endDate = endDate,
        roomId = roomId,
        userId = userId
    )
}