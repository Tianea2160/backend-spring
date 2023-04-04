package com.teamteam.backend.shared.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.teamteam.backend.shared.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint(
    private val mapper : ObjectMapper
) :AuthenticationEntryPoint{
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val res = ErrorResponse(
            message = "authentication error",
            code = "authentication_error",
            status = 401)

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = 401
        response.writer.print(mapper.writeValueAsString(res))
    }
}