package com.teamteam.backend.domain.reservation.repository

import com.teamteam.backend.domain.reservation.entity.ReservationTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReservationTimeRepository : JpaRepository<ReservationTime, String> {
    fun findAllBySummaryIdIn(summaryIds : List<String>): List<ReservationTime>
    fun findAllBySummaryId(summaryId : String): List<ReservationTime>
    fun deleteAllBySummaryId(summaryId : String)
}