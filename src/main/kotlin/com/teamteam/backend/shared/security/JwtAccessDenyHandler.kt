package com.teamteam.backend.shared.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.teamteam.backend.shared.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class JwtAccessDenyHandler(
    private val mapper: ObjectMapper
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        val res = ErrorResponse(message = "no access permission", code = "access_deny", status = 403)
        response.writer.print(mapper.writeValueAsString(res))
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = 403
    }
}