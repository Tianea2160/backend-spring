package com.teamteam.backend.domain.room.error

import com.teamteam.backend.shared.dto.ErrorResponse
import com.teamteam.backend.shared.error.TeamTeamRuntimeException

class RoomCreateException : TeamTeamRuntimeException(
    ErrorResponse(
        message = "id not found error",
        code = "ROOM_CREATE_ERROR",
        status = 500
    )
) {
}