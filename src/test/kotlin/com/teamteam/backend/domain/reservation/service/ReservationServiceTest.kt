package com.teamteam.backend.domain.reservation.service

import com.teamteam.backend.domain.building.dto.BuildingReadSimpleDTO
import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.member.dto.MemberReadDTO
import com.teamteam.backend.domain.member.dto.MockMemberCreateDTO
import com.teamteam.backend.domain.member.service.MemberService
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryAdminCreateDTO
import com.teamteam.backend.domain.reservation.dto.ReservationTimeCreateDTO
import com.teamteam.backend.domain.reservation.entity.Reservation
import com.teamteam.backend.domain.reservation.entity.ReservationSummary
import com.teamteam.backend.domain.reservation.entity.ReservationTime
import com.teamteam.backend.domain.reservation.repository.ReservationRepository
import com.teamteam.backend.domain.reservation.repository.ReservationSummaryRepository
import com.teamteam.backend.domain.reservation.repository.ReservationTimeRepository
import com.teamteam.backend.domain.room.dto.RoomReadDTO
import com.teamteam.backend.domain.room.service.RoomService
import com.teamteam.backend.shared.security.User
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class ReservationServiceTest : BehaviorSpec({
    val provider = IdentifierProvider()
    val reservationSummaryRepository = mockk<ReservationSummaryRepository>()
    val reservationTimeRepository = mockk<ReservationTimeRepository>()
    val reservationRepository = mockk<ReservationRepository>()
    val roomService = mockk<RoomService>()
    val memberService = mockk<MemberService>()
    val reservationService = ReservationService(
        provider = provider,
        reservationSummaryRepository = reservationSummaryRepository,
        reservationTimeRepository = reservationTimeRepository,
        reservationRepository = reservationRepository,
        roomService = roomService,
        memberService = memberService
    )

    given("admin user") {
        val user = User(
            password = "",
            username = "관리자",
            id = "admin",
            role = "ADMIN",
            building = ""
        )

        `when`("request reservation summary create") {
            val mockUser = MockMemberCreateDTO(username = "test user")
            val dto = ReservationSummaryAdminCreateDTO(
                user = mockUser,
                activity = "test activity",
                startDate = LocalDate.of(2023, 10, 11),
                endDate = LocalDate.of(2023, 10, 11),
                times = listOf(
                    ReservationTimeCreateDTO(
                        startTime = LocalTime.of(10, 0, 0),
                        endTime = LocalTime.of(11, 0, 0),
                        dayOfWeek = DayOfWeek.MONDAY
                    ),
                )
            )
            every { reservationRepository.saveAll(any<List<Reservation>>()) } returns listOf()
            every { memberService.createMockMember(any()) } returns MemberReadDTO(
                id = "test mock id",
                username = mockUser.username
            )
            every { roomService.isExist(any()) } returns true
            every { roomService.isValid(any(), any()) } returns true
            every { reservationSummaryRepository.save(any()) } returns ReservationSummary(
                id = "test id",
                activity = dto.activity,
                startDate = dto.startDate,
                endDate = dto.endDate,
                isCreatedByAdmin = false,
                roomId = "test room id",
                userId = "test mock user id",
            )
            every { reservationTimeRepository.saveAll(any<List<ReservationTime>>()) } returns listOf(
                ReservationTime(
                    id = "test id",
                    startTime = dto.times[0].startTime,
                    endTime = dto.times[0].endTime,
                    dayOfWeek = dto.times[0].dayOfWeek,
                    summaryId = "test summary id",
                )
            )
            every { reservationRepository.existsReservationBetweenTimes(any(), any(), any()) } returns false

            every { roomService.findById("test room id") } returns RoomReadDTO(
                id = "test room id",
                name = "test room name",
                capacity = 10,
                building = BuildingReadSimpleDTO(
                    id = "test building id",
                    name = "test building name",
                    description = "test building description",
                ),
                description = "test description",
                equipments = setOf()
            )

            val result = reservationService.createReservationSummaryByAdmin(user, dto, roomId = "test room id")
            then("ReservationSummaryReadDTO should be returned") {
                result.activity shouldBe dto.activity
                result.startDate shouldBe dto.startDate
                result.endDate shouldBe dto.endDate
                result.room.id shouldBe "test room id"
                result.user.username shouldBe mockUser.username
                result.times.size shouldBe 1
                result.times[0].startTime shouldBe dto.times[0].startTime
                result.times[0].endTime shouldBe dto.times[0].endTime
                result.times[0].dayOfWeek shouldBe dto.times[0].dayOfWeek
            }
        }
    }
})