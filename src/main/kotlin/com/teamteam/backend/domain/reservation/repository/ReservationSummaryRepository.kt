package com.teamteam.backend.domain.reservation.repository

import com.teamteam.backend.domain.reservation.entity.ReservationStatus
import com.teamteam.backend.domain.reservation.entity.ReservationSummary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReservationSummaryRepository : JpaRepository<ReservationSummary, String> {
    fun findAllByUserId(userId: String): List<ReservationSummary>
    fun findAllByStatus(status: ReservationStatus): List<ReservationSummary>
}