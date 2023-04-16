package com.teamteam.backend.domain.reservation.repository

import com.teamteam.backend.domain.reservation.entity.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ReservationRepository : JpaRepository<Reservation, String> {
    @Query(
        nativeQuery = true,
        value = "SELECT IF(t.exist > 0, 'true', 'false') from (SELECT EXISTS(SELECT * FROM reservation WHERE room_id = ?1 AND ((start_time <= ?2 AND end_time > ?2) OR (start_time < ?3 AND end_time >= ?3)))as exist) as t"
    )
    fun existsReservationBetweenTimes(roomId: String, start: LocalDateTime, end: LocalDateTime): Boolean
    fun deleteAllBySummaryId(summaryId: String)
    fun findAllByUserId(userId: String): List<Reservation>
}