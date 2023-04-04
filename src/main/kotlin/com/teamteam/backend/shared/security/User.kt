package com.teamteam.backend.shared.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class User(
    val id : String,
    private val username:String,
    private val password : String,
    private val role : String,
    val building : String
) : UserDetails{
    // ROLE_{permission} is spring security 'authority' pattern
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableListOf(SimpleGrantedAuthority("ROLE_${role}"))
    override fun getPassword(): String = password
    override fun getUsername(): String = username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
    override fun toString(): String {
        return "User(id='$id', username='$username', password='$password', role='$role', building='$building')"
    }
}