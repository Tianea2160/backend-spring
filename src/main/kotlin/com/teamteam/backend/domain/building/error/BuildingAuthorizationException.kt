package com.teamteam.backend.domain.building.error

import com.teamteam.backend.shared.dto.ErrorResponse
import com.teamteam.backend.shared.error.TeamTeamRuntimeException


class BuildingAuthorizationException : TeamTeamRuntimeException(
    ErrorResponse(
        message = "no permission",
        code = "no_permission",
        status = 403
    )
)