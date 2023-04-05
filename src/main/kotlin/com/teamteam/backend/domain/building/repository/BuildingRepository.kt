package com.teamteam.backend.domain.building.repository

import com.teamteam.backend.domain.building.entity.Building
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BuildingRepository  :JpaRepository<Building, String>{
    fun existsByName(name : String) : Boolean
}