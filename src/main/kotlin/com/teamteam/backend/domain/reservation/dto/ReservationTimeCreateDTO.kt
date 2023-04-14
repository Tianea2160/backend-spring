package com.teamteam.backend.domain.reservation.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.teamteam.backend.domain.reservation.entity.ReservationTime
import java.time.DayOfWeek
import java.time.LocalTime


class ReservationTimeCreateDTO(
    @JsonFormat(pattern = "HH:mm:ss", shape = JsonFormat.Shape.STRING)
    val startTime: LocalTime,
    @JsonFormat(pattern = "HH:mm:ss", shape = JsonFormat.Shape.STRING)
    val endTime: LocalTime,
    val dayOfWeek: DayOfWeek
) {
    fun toEntity(id : String, summaryId: String) =
        ReservationTime(id = id, summaryId = summaryId, startTime = startTime, endTime = endTime, dayOfWeek = dayOfWeek)
}