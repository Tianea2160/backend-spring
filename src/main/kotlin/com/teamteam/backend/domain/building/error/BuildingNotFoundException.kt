package com.teamteam.backend.domain.building.error

import com.teamteam.backend.shared.dto.ErrorResponse
import com.teamteam.backend.shared.error.TeamTeamRuntimeException

class BuildingNotFoundException : TeamTeamRuntimeException(
    ErrorResponse(
        message = "building not found",
        code = "building_not_found",
        status = 404
    )
)