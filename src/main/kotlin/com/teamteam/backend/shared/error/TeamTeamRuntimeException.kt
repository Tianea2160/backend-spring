package com.teamteam.backend.shared.error

import com.teamteam.backend.shared.dto.ErrorResponse

open class TeamTeamRuntimeException(
    val error : ErrorResponse
) : RuntimeException()