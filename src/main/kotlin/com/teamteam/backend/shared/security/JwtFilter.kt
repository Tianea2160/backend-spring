package com.teamteam.backend.shared.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val bearer:String? = request.getHeader("Authorization")
        if(bearer == null){
            logger.info("bearer is null")
            filterChain.doFilter(request, response)
            return
        }
        val jwt:String = bearer.substring("Bearer ".length)
        val authentication = jwtService.createAuthenticationToken(jwt)
        SecurityContextHolder.getContext().authentication = authentication
        filterChain.doFilter(request, response)
    }
}