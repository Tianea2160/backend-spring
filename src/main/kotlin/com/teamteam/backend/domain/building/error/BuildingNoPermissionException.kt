package com.teamteam.backend.domain.building.error

import com.teamteam.backend.shared.dto.ErrorResponse
import com.teamteam.backend.shared.error.TeamTeamRuntimeException


class BuildingNoPermissionException : TeamTeamRuntimeException(
    ErrorResponse(
        message = "building no permission error",
        code = "BUILDING_NO_PERMISSION_ERROR",
        status = 500
    )
)
