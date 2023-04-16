package com.teamteam.backend.domain.reservation.service

import com.teamteam.backend.domain.building.error.BuildingNoPermissionException
import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.member.dto.MemberReadDTO
import com.teamteam.backend.domain.member.service.MemberService
import com.teamteam.backend.domain.reservation.dto.ReservationReadDTO
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryAdminCreateDTO
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryCreateDTO
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryReadDTO
import com.teamteam.backend.domain.reservation.entity.Reservation
import com.teamteam.backend.domain.reservation.entity.ReservationStatus
import com.teamteam.backend.domain.reservation.error.ReservationAlreadyExistException
import com.teamteam.backend.domain.reservation.error.ReservationNotFoundException
import com.teamteam.backend.domain.reservation.error.ReservationPermissionException
import com.teamteam.backend.domain.reservation.repository.ReservationRepository
import com.teamteam.backend.domain.reservation.repository.ReservationSummaryRepository
import com.teamteam.backend.domain.reservation.repository.ReservationTimeRepository
import com.teamteam.backend.domain.room.error.RoomNotFoundException
import com.teamteam.backend.domain.room.service.RoomService
import com.teamteam.backend.shared.security.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReservationService(
    private val reservationSummaryRepository: ReservationSummaryRepository,
    private val reservationTimeRepository: ReservationTimeRepository,
    private val memberService: MemberService,
    private val roomService: RoomService,
    private val provider: IdentifierProvider,
    private val reservationRepository: ReservationRepository
) {
    private val logger = LoggerFactory.getLogger(ReservationService::class.java)

    //*** read only logic ***//
    @Transactional(readOnly = true)
    fun findAll(): List<ReservationSummaryReadDTO> {
        val summarys = reservationSummaryRepository.findAll()
        return summarys.map { summary ->
            val times = reservationTimeRepository.findAllBySummaryId(summary.id)
            val member = memberService.findById(summary.userId, summary.isCreatedByAdmin)
            val room = roomService.findById(summary.roomId)
            ReservationSummaryReadDTO.from(member, summary, room, times)
        }
    }

    @Transactional(readOnly = true)
    fun findAllNeedPermit(): List<ReservationSummaryReadDTO> {
        val summarys = reservationSummaryRepository.findAllByStatus(ReservationStatus.PENDING)
        val times = reservationTimeRepository.findAllBySummaryIdIn(summarys.map { it.id })
        return summarys.map { summary ->
            val member = memberService.findById(summary.userId, summary.isCreatedByAdmin)
            val room = roomService.findById(summary.roomId)
            ReservationSummaryReadDTO.from(member, summary, room, times.filter { time -> time.summaryId == summary.id })
        }
    }

    @Transactional(readOnly = true)
    fun findMyReservationSummarys(user: User): List<ReservationSummaryReadDTO> {
        val summarys = reservationSummaryRepository.findAllByUserId(user.id)
        val times = reservationTimeRepository.findAllBySummaryIdIn(summarys.map { it.id })

        return summarys.map { summary ->
            val member = MemberReadDTO.from(user)
            val room = roomService.findById(summary.roomId)
            ReservationSummaryReadDTO.from(member, summary, room, times.filter { time -> time.summaryId == summary.id })
        }
    }

    @Transactional(readOnly = true)
    fun findMyReservations(user: User): List<ReservationReadDTO> {
        val reservations = reservationRepository.findAllByUserId(user.id)
        val member = MemberReadDTO.from(user)
        return reservations.map { reservation -> ReservationReadDTO.from(reservation, member) }
    }

    //*** command logic ***//

    @Transactional
    fun createReservationSummary(
        user: User,
        roomId: String,
        dto: ReservationSummaryCreateDTO
    ): ReservationSummaryReadDTO {
        if (!roomService.isExist(roomId)) throw RoomNotFoundException()
        val roomReadDTO = roomService.findById(roomId)
        val memberReadDTO = MemberReadDTO.from(user)
        val summary = dto.toEntity(id = provider.generate(), roomId = roomId, userId = user.id)
            .let { summary -> reservationSummaryRepository.save(summary) }

        val times = dto.times.map { time ->
            time.toEntity(id = provider.generate(), summaryId = summary.id)
        }.let { times -> reservationTimeRepository.saveAll(times) }

        return ReservationSummaryReadDTO.from(memberReadDTO, summary, roomReadDTO, times)
    }

    @Transactional
    fun createReservationSummaryByAdmin(
        user: User,
        dto: ReservationSummaryAdminCreateDTO,
        roomId: String
    ): ReservationSummaryReadDTO {
        val mockMember = memberService.createMockMember(dto.user.username)
        if (!roomService.isExist(roomId)) throw RoomNotFoundException()
        if (!roomService.isValid(roomId, user.id)) throw BuildingNoPermissionException()

        val summary = dto.toEntity(id = provider.generate(), roomId = roomId, userId = mockMember.id)

        summary.isCreatedByAdmin = true
        summary.approve()

        val roomDTO = roomService.findById(roomId)
        reservationSummaryRepository.save(summary)

        val times = dto.times.map { time ->
            time.toEntity(id = provider.generate(), summaryId = summary.id)
        }.let { times -> reservationTimeRepository.saveAll(times) }

        val reservations = mutableListOf<Reservation>()
        val dayOfWeeks = dto.times.map { it.dayOfWeek }.distinct()

        for (day in dto.startDate.datesUntil(dto.endDate.plusDays(1))) {
            for (time in times) {
                if (day.dayOfWeek in dayOfWeeks) {
                    val reservation = Reservation(
                        id = provider.generate(),
                        summaryId = summary.id,
                        activity = summary.activity,
                        userId = mockMember.id,
                        roomId = roomId,
                        startTime = LocalDateTime.of(day, time.startTime),
                        endTime = LocalDateTime.of(day, time.endTime),
                        dayOfWeek = day.dayOfWeek,
                    )

                    if (reservationRepository.existsReservationBetweenTimes(
                            roomId = reservation.roomId,
                            start = reservation.startTime,
                            end = reservation.endTime
                        )
                    ) {
                        throw ReservationAlreadyExistException(reservation.startTime, reservation.endTime)
                    }

                    reservations.add(reservation)
                }
            }
        }
        reservationRepository.saveAll(reservations)
        return ReservationSummaryReadDTO.from(mockMember, summary, roomDTO, times)
    }

    @Transactional
    fun deleteReservationSummaryByAdmin(summaryId: String) {
        val summary =
            reservationSummaryRepository.findById(summaryId).orElseThrow { throw ReservationNotFoundException() }
        reservationTimeRepository.deleteAllBySummaryId(summary.id)
        reservationRepository.deleteAllBySummaryId(summary.id)
        reservationSummaryRepository.deleteById(summary.id)
        if (summary.isCreatedByAdmin)
            memberService.deleteMockMemberById(summary.userId)
    }

    @Transactional
    fun deleteReservation(user: User, reservationId: String) {
        val reservation =
            reservationRepository.findById(reservationId).orElseThrow { throw ReservationNotFoundException() }
        if (reservation.userId != user.id) throw ReservationPermissionException()
        reservationRepository.deleteById(reservationId)
    }

    @Transactional
    fun deleteReservationByAdmin(reservationId: String) {
        reservationRepository.deleteById(reservationId)
    }

    @Transactional
    fun deleteReservationSummary(user: User, summaryId: String) {
        val summary =
            reservationSummaryRepository.findById(summaryId).orElseThrow { throw ReservationNotFoundException() }
        if (summary.userId != user.id) throw ReservationPermissionException()
        reservationTimeRepository.deleteAllBySummaryId(summary.id)
        reservationRepository.deleteAllBySummaryId(summary.id)
        reservationSummaryRepository.deleteById(summary.id)
    }

    @Transactional
    fun permitReservationSummaryByAdmin(user: User, summaryId: String) {
        val summary =
            reservationSummaryRepository.findById(summaryId).orElseThrow { throw ReservationNotFoundException() }
        val times = reservationTimeRepository.findAllBySummaryId(summary.id)
        summary.approve()

        val reservations = summary.createReservations(provider, times)
        reservations.forEach { res ->
            if (reservationRepository.existsReservationBetweenTimes(
                    start = res.startTime,
                    end = res.endTime,
                    roomId = res.roomId
                )
            ) throw ReservationAlreadyExistException(res.startTime, res.endTime)
        }
        reservationRepository.saveAll(reservations)
    }
}