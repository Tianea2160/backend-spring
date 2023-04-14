package com.teamteam.backend.domain.reservation.error

import com.teamteam.backend.shared.dto.ErrorResponse
import com.teamteam.backend.shared.error.TeamTeamRuntimeException

class ReservationPermissionException : TeamTeamRuntimeException(
    ErrorResponse(
        code = "reservation.permission",
        message = "You don't have permission to access this reservation.",
        status = 403
    )
)