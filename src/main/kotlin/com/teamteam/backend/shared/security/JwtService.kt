package com.teamteam.backend.shared.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secretKey: String
) {
    private val key = Keys.hmacShaKeyFor(secretKey.encodeToByteArray())

    fun createAuthenticationToken(jwt: String): Authentication {
        val parser = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()

        val claims = parser.parseClaimsJws(jwt).body
        val username = claims["username"] as String
        val id = claims["pid"] as String
        val role = claims["role"] as String

        val user = if (role.startsWith("ADMIN")) {
            val words = role.split("_")
            User(id = id, username = username, password = "", role = words[0], building = words[1])
        } else {
            User(id = id, username = username, password = "", role = role, building = "")
        }
        return UsernamePasswordAuthenticationToken(user, null, user.authorities)
    }
}