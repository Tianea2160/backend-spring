package com.teamteam.backend.domain.building.error

import com.teamteam.backend.shared.dto.ErrorResponse
import com.teamteam.backend.shared.error.TeamTeamRuntimeException

class BuildingNameConflictException : TeamTeamRuntimeException(
    ErrorResponse(
        message = "building name conflict",
        code = "building_name_conflict",
        status = 409
    )
)
