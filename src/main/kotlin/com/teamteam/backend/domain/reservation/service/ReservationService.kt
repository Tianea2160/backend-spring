package com.teamteam.backend.domain.reservation.service

import com.teamteam.backend.domain.building.error.BuildingNoPermissionException
import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryCreateDTO
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryReadDTO
import com.teamteam.backend.domain.reservation.error.ReservationNotFoundException
import com.teamteam.backend.domain.reservation.repository.ReservationSummaryRepository
import com.teamteam.backend.domain.reservation.repository.ReservationTimeRepository
import com.teamteam.backend.domain.room.error.RoomNotFoundException
import com.teamteam.backend.domain.room.repository.RoomRepository
import com.teamteam.backend.domain.room.service.RoomService
import com.teamteam.backend.domain.member.service.MemberService
import com.teamteam.backend.domain.member.service.MockMemberService
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryAdminCreateDTO
import com.teamteam.backend.domain.reservation.entity.Reservation
import com.teamteam.backend.domain.reservation.error.ReservationAlreadyExistException
import com.teamteam.backend.domain.reservation.repository.ReservationRepository
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
            s.id ?: throw ReservationNotFoundException()
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
        val summary = dto.toEntity(user.id, roomId)
        summary.id = provider.generate()
        reservationSummaryRepository.save(summary)

        val summaryId = summary.id ?: throw ReservationNotFoundException()
        val times = dto.times.map { t ->
            val entity = t.toEntity(summaryId)
            entity.id = provider.generate()
            entity
        }.let { times -> reservationTimeRepository.saveAll(times) }
        val roomReadDTO = roomService.findById(roomId)
        val memberReadDTO = memberService.findById(user.id)

        return ReservationSummaryReadDTO.from(memberReadDTO, summary, roomReadDTO, times)
    }

    @Transactional
    fun createReservationSummaryByAdmin(
        user : User,
        dto: ReservationSummaryAdminCreateDTO,
        roomId: String
    ): ReservationSummaryReadDTO {
        val mockMember = mockMemberService.createMockMember(dto.user.username)
        logger.info("mock user create $mockMember")

        if (!roomService.isExist(roomId)) throw RoomNotFoundException()
        if(!roomService.isValid(roomId,user.id)) throw BuildingNoPermissionException()
        logger.info("reservation user authentication check")


        val summary = dto.toEntity(roomId, mockMember.id)
        val roomDTO = roomService.findById(roomId)
        val summaryId = provider.generate()
        summary.id = summaryId
        reservationSummaryRepository.save(summary)

        val times = dto.times.map { t ->
            val time = t.toEntity(summaryId)
            time.id = provider.generate()
            time
        }.let { times -> reservationTimeRepository.saveAll(times) }

        // 관리자 권한 예약 생성이라서 예약 즉시 승인 처리
        summary.approve()
        logger.info("reservation summary, times save complete")

        val reservations = mutableListOf<Reservation>()
        val dayOfWeeks = dto.times.map { it.dayOfWeek }.distinct()

        for(day in dto.startDate.datesUntil(dto.endDate.plusDays(1))){
            for(time in times){
                if(day.dayOfWeek in dayOfWeeks){
                    val reservation = Reservation(
                        id = provider.generate(),
                        summaryId = summaryId,
                        activity = summary.activity,
                        userId = mockMember.id,
                        roomId = roomId,
                        startTime = LocalDateTime.of(day, time.startTime),
                        endTime = LocalDateTime.of(day, time.endTime),
                        dayOfWeek = day.dayOfWeek,
                    )

                    if(reservationRepository.existsReservationBetweenTimes(reservation.roomId, reservation.startTime, reservation.endTime)){
                        throw ReservationAlreadyExistException(reservation.startTime, reservation.endTime)
                    }

                    reservations.add(reservation)
                }
            }
        }

        logger.info("reservation valid check complete")


        reservationRepository.saveAll(reservations)
        logger.info("reservations save complete")
        return ReservationSummaryReadDTO.from(mockMember, summary, roomDTO, times)
    }
}