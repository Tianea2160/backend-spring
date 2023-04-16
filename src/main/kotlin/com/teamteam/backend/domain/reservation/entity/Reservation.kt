package com.teamteam.backend.domain.reservation.entity

import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.reservation.entity.ReservationStatus.*
import com.teamteam.backend.shared.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "reservation_summary")
class ReservationSummary(
    @Id
    var id: String,
    @Column(name = "room_id")
    var roomId: String,
    @Column(name = "user_id")
    var userId: String,
    var activity: String,
    @Enumerated(EnumType.STRING)
    var status: ReservationStatus = PENDING,
    @Column(name = "start_date")
    var startDate: LocalDate,
    @Column(name = "end_date")
    var endDate: LocalDate,
    @Column(name = "is_created_by_admin")
    var isCreatedByAdmin: Boolean = false,
    @Column(name = "approved_at")
    var approvedAt: LocalDateTime? = null
) : BaseTimeEntity() {

    fun approve() {
        status = APPROVED
        this.approvedAt = LocalDateTime.now()
    }

    fun reject() {
        status = REJECTED
    }

    fun cancel() {
        status = CANCELED
    }

    fun createReservations(provider: IdentifierProvider, times: List<ReservationTime>): MutableList<Reservation> {

        val start = this.startDate
        val end = this.endDate

        val reservations = mutableListOf<Reservation>()

        for (day in start.datesUntil(end.plusDays(1))) {
            for (time in times) {
                if (day.dayOfWeek != time.dayOfWeek) continue

                val reservation = Reservation(
                    id = provider.generate(),
                    summaryId = this.id,
                    activity = this.activity,
                    userId = this.userId,
                    roomId = this.roomId,
                    startTime = LocalDateTime.of(day, time.startTime),
                    endTime = LocalDateTime.of(day, time.endTime),
                    dayOfWeek = day.dayOfWeek,
                )
                reservations.add(reservation)
            }
        }
        return reservations
    }
}

enum class ReservationStatus {
    PENDING, APPROVED, REJECTED, CANCELED
}

@Entity
@Table(name = "reservation_time")
class ReservationTime(
    @Id
    var id: String,
    @Column(name = "summary_id")
    var summaryId: String,
    @Column(name = "start_time")
    var startTime: LocalTime,
    @Column(name = "end_time")
    var endTime: LocalTime,
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    var dayOfWeek: DayOfWeek
)

@Entity
@Table(name = "reservation")
class Reservation(
    @Id
    var id: String,
    @Column(name = "summary_id")
    var summaryId: String,
    @Column(name = "room_id")
    var roomId: String,
    @Column(name = "user_id")
    var userId: String,
    var activity: String,
    @Column(name = "start_time")
    var startTime: LocalDateTime,
    @Column(name = "end_time")
    var endTime: LocalDateTime,
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    var dayOfWeek: DayOfWeek
)