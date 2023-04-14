package com.teamteam.backend.domain.reservation.error

import com.teamteam.backend.shared.dto.ErrorResponse
import com.teamteam.backend.shared.error.TeamTeamRuntimeException
import java.time.LocalDateTime

class ReservationAlreadyExistException(start: LocalDateTime, end: LocalDateTime) : TeamTeamRuntimeException(
    ErrorResponse(
        message = "reservation already exist : $start ~ $end",
        code = "RESERVATION_ALREADY_EXIST",
        status = 409
    )
)