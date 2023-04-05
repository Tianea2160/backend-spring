package com.teamteam.backend.domain.generator

import org.springframework.stereotype.Service
import java.util.*

@Service
class IdentifierProvider {
    fun generate() : String = UUID.randomUUID().toString()
}