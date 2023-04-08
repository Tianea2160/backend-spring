package com.teamteam.backend.domain.equipment.error

import com.teamteam.backend.shared.dto.ErrorResponse
import com.teamteam.backend.shared.error.TeamTeamRuntimeException

class EquipmentNotFoundException : TeamTeamRuntimeException(
    ErrorResponse(
        code = "EQUIPMENT_NOT_FOUND",
        message = "해당하는 장비를 찾을 수 없습니다.",
        status = 404
    )
)