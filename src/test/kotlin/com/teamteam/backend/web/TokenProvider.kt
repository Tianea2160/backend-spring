package com.teamteam.backend.web

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.stereotype.Service

@TestConfiguration
class TokenProvider(
    @Value("\${jwt.secret}")
    private val secret: String,
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun createCookie(id :String, username: String, role: String): Cookie {
        val token = Jwts.builder()
            .setSubject(username)
            .claim("pid", id)
            .claim("username", username)
            .claim("role", role)
            .signWith(key)
            .compact()
        return Cookie("cat", token).apply {
            maxAge = 60 * 60 * 24 * 7
            path = "/"
        }
    }
}