package com.teamteam.backend.domain.reservation.error

import com.teamteam.backend.shared.dto.ErrorResponse
import com.teamteam.backend.shared.error.TeamTeamRuntimeException

class ReservationNotFoundException : TeamTeamRuntimeException(
    ErrorResponse(
        message = "Reservation not found",
        code = "reservation_not_found",
        status = 404
    )
)