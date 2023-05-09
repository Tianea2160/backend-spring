package com.teamteam.backend.domain.reservation.service

import com.teamteam.backend.domain.building.dto.BuildingReadDTO
import com.teamteam.backend.domain.building.dto.BuildingReadSimpleDTO
import com.teamteam.backend.domain.building.dto.ManagerReadDTO
import com.teamteam.backend.domain.building.service.BuildingService
import com.teamteam.backend.domain.generator.IdentifierProvider
import com.teamteam.backend.domain.member.dto.MemberReadDTO
import com.teamteam.backend.domain.member.dto.MockMemberCreateDTO
import com.teamteam.backend.domain.member.service.MemberService
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryAdminCreateDTO
import com.teamteam.backend.domain.reservation.dto.ReservationSummaryCreateDTO
import com.teamteam.backend.domain.reservation.dto.ReservationTimeCreateDTO
import com.teamteam.backend.domain.reservation.entity.Reservation
import com.teamteam.backend.domain.reservation.entity.ReservationStatus
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
import io.mockk.verify
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class ReservationServiceTest : BehaviorSpec({
    val provider = IdentifierProvider()
    val reservationSummaryRepository = mockk<ReservationSummaryRepository>()
    val reservationTimeRepository = mockk<ReservationTimeRepository>()
    val reservationRepository = mockk<ReservationRepository>()
    val roomService = mockk<RoomService>()
    val memberService = mockk<MemberService>()
    val buildingService = mockk<BuildingService>()
    val reservationService = ReservationService(
        provider = provider,
        reservationSummaryRepository = reservationSummaryRepository,
        reservationTimeRepository = reservationTimeRepository,
        reservationRepository = reservationRepository,
        roomService = roomService,
        memberService = memberService,
        buildingService = buildingService
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
        `when`("관리자 전용 예약 요약 삭제를 할 때") {
            val summaryId = "summaryId"
            every { reservationSummaryRepository.existsById(summaryId) } returns true
            every {
                reservationSummaryRepository.deleteById(summaryId)
                reservationTimeRepository.deleteAllBySummaryId(summaryId)
                reservationRepository.deleteAllBySummaryId(summaryId)
            } returns Unit
            every { reservationSummaryRepository.findById(summaryId) } returns Optional.of(
                ReservationSummary(
                    id = summaryId,
                    activity = "test activity",
                    startDate = LocalDate.of(2023, 10, 11),
                    endDate = LocalDate.of(2023, 10, 11),
                    isCreatedByAdmin = false,
                    roomId = "test room id",
                    userId = "test mock user id",
                )
            )

            reservationService.deleteReservationSummaryByAdmin(summaryId)

            then("예약 요약이 삭제되어야 한다.") {
                verify(exactly = 1) { reservationSummaryRepository.deleteById(summaryId) }
            }
        }

        `when`("관리자 전용 예약 단건 삭제를 할 때") {
            val reservationId = "reservationId"

            every { reservationRepository.existsById(reservationId) } returns true
            every { reservationRepository.deleteById(reservationId) } returns Unit
            every { reservationRepository.deleteAllBySummaryId(any()) } returns Unit
            every { reservationTimeRepository.deleteAllBySummaryId(any()) } returns Unit

            reservationService.deleteReservationByAdmin(reservationId)

            then("예약이 삭제되어야 한다.") {
                verify(exactly = 1) { reservationRepository.deleteById(reservationId) }
                verify(exactly = 1) { reservationRepository.deleteAllBySummaryId(any()) }
                verify(exactly = 1) { reservationTimeRepository.deleteAllBySummaryId(any()) }
            }
        }

        `when`("예약 승인이 필요한 리스트를 조회할 때") {
            val summarys = listOf(
                ReservationSummary(
                    id = "id1",
                    activity = "test activity",
                    startDate = LocalDate.of(2023, 10, 11),
                    endDate = LocalDate.of(2023, 10, 11),
                    isCreatedByAdmin = false,
                    roomId = "test room id",
                    userId = "test mock user id",
                ),
                ReservationSummary(
                    id = "id2",
                    activity = "test activity",
                    startDate = LocalDate.of(2023, 10, 11),
                    endDate = LocalDate.of(2023, 10, 11),
                    isCreatedByAdmin = false,
                    roomId = "test room id",
                    userId = "test mock user id",
                )
            )

            val times = listOf(
                ReservationTime(
                    id = "id3",
                    startTime = LocalTime.of(10, 0, 0),
                    endTime = LocalTime.of(11, 0, 0),
                    dayOfWeek = DayOfWeek.MONDAY,
                    summaryId = "id1",
                ),
                ReservationTime(
                    id = "id4",
                    startTime = LocalTime.of(10, 0, 0),
                    endTime = LocalTime.of(11, 0, 0),
                    dayOfWeek = DayOfWeek.MONDAY,
                    summaryId = "id2",
                )
            )

            every { reservationSummaryRepository.findAllByStatus(ReservationStatus.PENDING) } returns summarys
            every { reservationTimeRepository.findAllBySummaryIdIn(summarys.map { it.id }) } returns times
            every { memberService.findById(any()) } returns MemberReadDTO(
                id = "test mock id",
                username = "test mock username"
            )

            val reads = reservationService.findAllNeedPermit()

            then("예약 요약 리스트가 반환되어야 한다.") {
                reads.size shouldBe 2
                reads[0].id shouldBe summarys[0].id
                reads[0].times.size shouldBe 1
                reads[0].times[0].id shouldBe times[0].id
                reads[1].id shouldBe summarys[1].id
                reads[1].times.size shouldBe 1
                reads[1].times[0].id shouldBe times[1].id
            }
        }
    }

    given("예약이 있을 때") {
        val summarys = listOf(
            ReservationSummary(
                id = "id1",
                activity = "test activity",
                startDate = LocalDate.of(2023, 10, 11),
                endDate = LocalDate.of(2023, 10, 11),
                isCreatedByAdmin = false,
                roomId = "test room id",
                userId = "test mock user id",
            ),
            ReservationSummary(
                id = "id2",
                activity = "test activity",
                startDate = LocalDate.of(2023, 10, 11),
                endDate = LocalDate.of(2023, 10, 11),
                isCreatedByAdmin = false,
                roomId = "test room id",
                userId = "test mock user id",
            )
        )
        `when`("예약 전체 조회를 하면") {

            every { reservationSummaryRepository.findAll() } returns summarys
            every { memberService.findById(any()) } returns MemberReadDTO(
                id = "test mock id",
                username = "test mock username"
            )
            every { roomService.findById(any()) } returns RoomReadDTO(
                id = "test mock id",
                name = "test mock name",
                description = "test mock description",
                capacity = 10,
                equipments = setOf(),
                building = BuildingReadSimpleDTO(
                    id = "test mock id",
                    name = "test mock name",
                    description = "test mock description",
                )
            )
            every { reservationTimeRepository.findAllBySummaryId(any()) } returns listOf(
                ReservationTime(
                    id = "id3",
                    startTime = LocalTime.of(10, 0, 0),
                    endTime = LocalTime.of(11, 0, 0),
                    dayOfWeek = DayOfWeek.MONDAY,
                    summaryId = "id1",
                ),
                ReservationTime(
                    id = "id4",
                    startTime = LocalTime.of(10, 0, 0),
                    endTime = LocalTime.of(11, 0, 0),
                    dayOfWeek = DayOfWeek.MONDAY,
                    summaryId = "id2",
                )
            )

            val reads = reservationService.findAll()

            then("예약 리스트가 반환되어야한다.") {
                reads.size shouldBe 2
                reads[0].activity shouldBe summarys[0].activity
                reads[0].times.size shouldBe 2
                reads[1].activity shouldBe summarys[1].activity
                reads[1].times.size shouldBe 2
            }
        }
    }
    given("예약 승인 대기 요청이 있을 때") {
        val summary = ReservationSummary(
            id = "id1",
            activity = "test activity",
            startDate = LocalDate.of(2023, 10, 11),
            endDate = LocalDate.of(2023, 10, 11),
            isCreatedByAdmin = false,
            roomId = "test room id",
            userId = "test mock user id",
        )
        `when`("예약을 승인하면") {
            val s: ReservationSummary = mockk(relaxed = true)
            every { reservationSummaryRepository.findById(any()) } returns Optional.of(s)
            every { reservationSummaryRepository.save(any()) } returns summary
            every { reservationTimeRepository.findAllBySummaryId(any()) } returns listOf()
            every { reservationRepository.saveAll(any<List<Reservation>>()) } returns listOf()

            val user = User("adminId", "admin", "", "ADMIN", "building")
            reservationService.permitReservationSummaryByAdmin(user = user, summaryId = "id1")

            then("예약 상태가 승인으로 변경되고 예약들이 생성되며 저장되어야한다.") {
                verify(exactly = 1) { s.approve() }
            }
        }
    }

    given("나만의 예약들이 있을때") {

        val summary = ReservationSummary(
            id = "id1",
            activity = "test activity",
            startDate = LocalDate.of(2023, 10, 11),
            endDate = LocalDate.of(2023, 10, 11),
            isCreatedByAdmin = false,
            roomId = "test room id",
            userId = "test mock user id",
        )
        val reservation = Reservation(
            id = "id1",
            summaryId = "id1",
            userId = "test mock user id",
            roomId = "test room id",
            startTime = LocalDateTime.of(2023, 10, 11, 10, 0, 0),
            endTime = LocalDateTime.of(2023, 10, 11, 11, 0, 0),
            activity = "test activity",
            dayOfWeek = DayOfWeek.MONDAY,
        )
        val user = User("userId", "admin", "", "ADMIN", "building")

        `when`("나의 예약 전체 조회를 호출하면") {
            every { reservationSummaryRepository.findAllByUserId(user.id) } returns listOf(summary)
            every { memberService.findById(user.id) } returns MemberReadDTO(
                id = user.id,
                username = "test mock username"
            )
            every { roomService.findById(any()) } returns RoomReadDTO(
                id = "test mock id",
                name = "test mock name",
                description = "test mock description",
                capacity = 10,
                equipments = setOf(),
                building = BuildingReadSimpleDTO(
                    id = "test mock id",
                    name = "test mock name",
                    description = "test mock description",
                )
            )
            every { reservationTimeRepository.findAllBySummaryIdIn(any()) } returns listOf(
                ReservationTime(
                    id = "id3",
                    startTime = LocalTime.of(10, 0, 0),
                    endTime = LocalTime.of(11, 0, 0),
                    dayOfWeek = DayOfWeek.MONDAY,
                    summaryId = "id1",
                )
            )

            val reads = reservationService.findMyReservationSummarys(user)
            then("나의 예약 요약 들이 조회되어야한다.") {
                reads.size shouldBe 1
                reads[0].activity shouldBe summary.activity
                reads[0].times.size shouldBe 1
            }
        }

        `when`("나의 예약 상세 조회를 호출하면") {
            every { reservationRepository.findAllByUserId(user.id) } returns listOf(reservation)
            every { memberService.findById(user.id) } returns MemberReadDTO(
                id = user.id,
                username = "test mock username"
            )
            every { roomService.findById(any()) } returns RoomReadDTO(
                id = "test mock id",
                name = "test mock name",
                description = "test mock description",
                capacity = 10,
                equipments = setOf(),
                building = BuildingReadSimpleDTO(
                    id = "test mock id",
                    name = "test mock name",
                    description = "test mock description",
                )
            )
            every { buildingService.findById(any()) } returns BuildingReadDTO(
                id = "test mock id",
                name = "test mock name",
                description = "test mock description",
                location = "test mock location",
                imageUrl = "test mock image url",
                manager = ManagerReadDTO(
                    id = "test mock id",
                    username = "test mock username",
                )
            )
            val reads = reservationService.findMyReservations(user = user)

            then("나의 예약 상세가 조회되어야한다.") {
                reads.size shouldBe 1
            }
        }
    }
    given("일반 사용자가") {
        val user = User("userId", "student", "", "STUDENT", "building")

        `when`("예약을 요청하게 되면") {
            val roomId = "roomId"
            val create = ReservationSummaryCreateDTO(
                activity = "test activity",
                times = listOf(
                    ReservationTimeCreateDTO(
                        startTime = LocalTime.of(10, 0, 0),
                        endTime = LocalTime.of(11, 0, 0),
                        dayOfWeek = DayOfWeek.MONDAY,
                    )
                ),
                startDate = LocalDate.of(2023, 10, 11),
                endDate = LocalDate.of(2023, 10, 11),
            )
            every { roomService.findById(roomId) } returns RoomReadDTO(
                id = "test mock id",
                name = "test mock name",
                description = "test mock description",
                capacity = 10,
                equipments = setOf(),
                building = BuildingReadSimpleDTO(
                    id = "test mock id",
                    name = "test mock name",
                    description = "test mock description",
                )
            )
            every { reservationSummaryRepository.save(any()) } returns ReservationSummary(
                id = "id1",
                activity = "test activity",
                startDate = LocalDate.of(2023, 10, 11),
                endDate = LocalDate.of(2023, 10, 11),
                isCreatedByAdmin = false,
                roomId = "test room id",
                userId = "test mock user id",
            )
            every { reservationTimeRepository.saveAll(any<List<ReservationTime>>()) } returns listOf(
                ReservationTime(
                    id = "id3",
                    startTime = LocalTime.of(10, 0, 0),
                    endTime = LocalTime.of(11, 0, 0),
                    dayOfWeek = DayOfWeek.MONDAY,
                    summaryId = "id1",
                )
            )

            val read = reservationService.createReservationSummary(user = user, dto = create, roomId = roomId)

            then("예약 요약이 생성되고 승인 대기 상태로 저장되어야한다.") {
                read.activity shouldBe create.activity
                read.times.size shouldBe 1
                read.times[0].startTime shouldBe create.times[0].startTime
                read.times[0].endTime shouldBe create.times[0].endTime
                read.times[0].dayOfWeek shouldBe create.times[0].dayOfWeek
                read.startDate shouldBe create.startDate
                read.endDate shouldBe create.endDate
            }
        }
    }

    given("예약이 있을 때") {
        val user = User("userId", "admin", "", "ADMIN", "building")
        val summary = ReservationSummary(
            id = "id1",
            activity = "test activity",
            startDate = LocalDate.of(2023, 10, 11),
            endDate = LocalDate.of(2023, 10, 11),
            isCreatedByAdmin = false,
            roomId = "test room id",
            userId = user.id,
        )

        val reservation = Reservation(
            id = "id2",
            activity = "test activity",
            startTime = LocalDateTime.of(2023, 10, 11, 10, 0, 0),
            endTime = LocalDateTime.of(2023, 10, 11, 11, 0, 0),
            summaryId = "id1",
            userId = user.id,
            dayOfWeek = DayOfWeek.MONDAY,
            roomId = "roomId"
        )

        `when`("예약 요약을 삭제하면") {
            every { reservationTimeRepository.deleteAllBySummaryId(summary.id) } returns Unit
            every { reservationSummaryRepository.deleteById(summary.id) } returns Unit
            every { reservationRepository.deleteAllBySummaryId(summary.id) } returns Unit
            every { reservationSummaryRepository.findById(summary.id) } returns Optional.of(summary)
            reservationService.deleteReservationSummary(user, summary.id)

            then("예약 요약이 삭제된다.") {
                verify(exactly = 1) { reservationSummaryRepository.deleteById(summary.id) }
                verify(exactly = 1) { reservationRepository.deleteAllBySummaryId(summary.id) }
                verify(exactly = 1) { reservationTimeRepository.deleteAllBySummaryId(summary.id) }
            }
        }
        `when`("사용자가 예약을 삭제하면") {
            every { reservationRepository.deleteById(reservation.id) } returns Unit
            every { reservationRepository.findById(reservation.id) } returns Optional.of(reservation)
            reservationService.deleteReservation(user, reservation.id)

            then("예약이 삭제된다.") {
                verify(exactly = 1) { reservationRepository.deleteById(reservation.id) }
            }
        }
    }
})