package com.teamteam.backend.domain.test

import com.teamteam.backend.shared.security.User
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/test/admin")
    fun admin(authentication:Authentication):String{
        val user = authentication.principal as? User ?:  throw IllegalArgumentException("authentication exception")
        return "$user"
    }

    @GetMapping("/test/student")
    fun student() : String = "student"

    @GetMapping("/ping")
    fun ping() = "ping"
}