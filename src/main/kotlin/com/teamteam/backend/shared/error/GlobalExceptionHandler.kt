package com.teamteam.backend.shared.error

import com.teamteam.backend.shared.dto.ErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(value = [TeamTeamRuntimeException::class])
    fun teamTeamRuntimeExceptionHandler(e: TeamTeamRuntimeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(e.error.status).body(e.error)
    }
}