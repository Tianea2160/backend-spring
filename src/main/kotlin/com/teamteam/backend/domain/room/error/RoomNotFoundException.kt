package com.teamteam.backend.domain.room.error

import com.teamteam.backend.shared.dto.ErrorResponse
import com.teamteam.backend.shared.error.TeamTeamRuntimeException

class RoomNotFoundException : TeamTeamRuntimeException(
    ErrorResponse(
        message = "room not found error",
        code = "ROOM_NOT_FOUND_ERROR",
        status = 500
    )
) {
}