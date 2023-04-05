package com.teamteam.backend.config

import com.teamteam.backend.shared.security.JwtAccessDenyHandler
import com.teamteam.backend.shared.security.JwtAuthenticationEntryPoint
import com.teamteam.backend.shared.security.JwtFilter
import com.teamteam.backend.shared.security.User
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtFilter: JwtFilter,
    private val jwtAccessDenyHandler: JwtAccessDenyHandler,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint
) {
    // override default user details service auto configuration for removing default user and log message
    @Bean
    fun userDetailsService() : UserDetailsService = UserDetailsService { username -> User("", username, "", "", "") }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors()
            .configurationSource(corsConfigurerSource())
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf().disable()
            .httpBasic().disable()
            .formLogin().disable()
            .authorizeHttpRequests()
            .requestMatchers("/ping").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/building").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/building/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/building/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/building/**").hasRole("ADMIN")
            .requestMatchers("/v3/api-docs/**", "/swagger/**", "/swagger-ui.html" , "/swagger-ui/**").permitAll()
            .anyRequest().denyAll() // deny all request that we are not using
            .and()
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling()
            .accessDeniedHandler(jwtAccessDenyHandler)
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun corsConfigurerSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedHeaders = listOf("*")
        config.allowedMethods = listOf("*")
        config.allowedOrigins = listOf("*")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}