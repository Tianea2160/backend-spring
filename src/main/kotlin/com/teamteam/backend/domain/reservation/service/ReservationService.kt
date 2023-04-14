package com.teamteam.backend.domain.reservation.service

import com.teamteam.backend.domain.building.error.BuildingNoPermissionException
import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.member.service.MemberService
import com.teamteam.backend.domain.member.service.MockMemberService
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryAdminCreateDTO
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryCreateDTO
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryReadDTO
import com.teamteam.backend.domain.reservation.entity.Reservation
import com.teamteam.backend.domain.reservation.error.ReservationAlreadyExistException
import com.teamteam.backend.domain.reservation.error.ReservationNotFoundException
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
    private val mockMemberService: MockMemberService,
    private val provider: IdentifierProvider,
    private val reservationRepository: ReservationRepository
) {
    private val logger = LoggerFactory.getLogger(ReservationService::class.java)

    //*** read only logic ***//
    @Transactional(readOnly = true)
    fun findMyReservationSummary(user: User): List<ReservationSummaryReadDTO> {
        val summaries = reservationSummaryRepository.findAllByUserId(user.id)
        val times = reservationTimeRepository.findAllBySummaryIdIn(summaries.map { s ->
            s.id
        })
        return summaries.map { summary ->
            val roomReadDTO = roomService.findById(summary.roomId)
            val member = memberService.findById(summary.userId)
            ReservationSummaryReadDTO.from(
                member,
                summary,
                roomReadDTO,
                times.filter { t -> t.summaryId == summary.id })
        }
    }

    //*** command logic ***//
    @Transactional
    fun createReservationSummary(
        user: User,
        roomId: String,
        dto: ReservationSummaryCreateDTO
    ): ReservationSummaryReadDTO {
        if (!roomService.isExist(roomId)) throw RoomNotFoundException()
        val summary = dto.toEntity(provider.generate(), user.id, roomId)
        summary.id = provider.generate()
        reservationSummaryRepository.save(summary)

        val times = dto.times.map { time ->
            time.toEntity(id = provider.generate(), summaryId = summary.id)
        }.let { times -> reservationTimeRepository.saveAll(times) }
        val roomReadDTO = roomService.findById(roomId)
        val memberReadDTO = memberService.findById(user.id)

        return ReservationSummaryReadDTO.from(memberReadDTO, summary, roomReadDTO, times)
    }

    @Transactional
    fun createReservationSummaryByAdmin(
        user: User,
        dto: ReservationSummaryAdminCreateDTO,
        roomId: String
    ): ReservationSummaryReadDTO {
        val mockMember = mockMemberService.createMockMember(dto.user.username)
        if (!roomService.isExist(roomId)) throw RoomNotFoundException()
        if (!roomService.isValid(roomId, user.id)) throw BuildingNoPermissionException()

        val summary = dto.toEntity(id = provider.generate(), roomId = roomId, userId = mockMember.id)
        val roomDTO = roomService.findById(roomId)
        reservationSummaryRepository.save(summary)

        val times = dto.times.map { time ->
            time.toEntity(id = provider.generate(), summaryId = summary.id)
        }.let { times -> reservationTimeRepository.saveAll(times) }

        summary.approve()

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
        mockMemberService.deleteMockMemberById(summary.userId)
    }
}