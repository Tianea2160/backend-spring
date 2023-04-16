package com.teamteam.backend.domain.member.error

import com.teamteam.backend.shared.dto.ErrorResponse
import com.teamteam.backend.shared.error.TeamTeamRuntimeException


class MemberNotFoundException : TeamTeamRuntimeException(
    ErrorResponse(
        code = "USER_NOT_FOUND",
        message = "User not found",
        status = 404
    )
)