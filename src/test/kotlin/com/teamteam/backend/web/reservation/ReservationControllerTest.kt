package com.teamteam.backend.web.reservation

import com.teamteam.backend.domain.reservation.service.ReservationService
import com.teamteam.backend.shared.security.JwtService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    @MockBean
    private val jwtService: JwtService,
    @MockBean
    private val reservationService: ReservationService
) {

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `학생은 예약 승인 대기 리스트를 조회할 수 없다`() {
        mockMvc.get("/api/reservation/admin/summary/permit")
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `학생은 관리자 전용 예약 승인 api를 사용할 수 없다`() {
        mockMvc.post("/api/reservation/admin/summary/permit/klfjsdklfjsklfjsdkl")
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `학생은 관리자 예약 요약 생성을 할 수 없다`() {
        mockMvc.post("/api/reservation/admin/summary/klfjsdklfjsklfjsdkl")
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `학생은 관리자전용 예약 전체 삭제를 사용할 수 없다`() {
        mockMvc.delete("/api/reservation/admin/summary/klfjsdklfjsklfjsdkl")
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `학생은 관리자전용 예약 단건 삭제를 사용할 수 없다`() {
        mockMvc.delete("/api/reservation/admin/klfjsdklfjsklfjsdkl")
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `관리자는 예약 승인 대기 리스트를 조회할 수 있다`() {
        mockMvc.get("/api/reservation/admin/summary/permit")
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `관리자는 일반 사용자 전용 예약 생성 api를 사용할 수 없다`() {
        mockMvc.post("/api/reservation/summary/klfjsdklfjsklfjsdkl")
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `관리자는 일반 사용자 전용 자신의 예약 요약 현황 조회를 사용할 수 없다`() {
        mockMvc.get("/api/reservation/summary/me")
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `관리자는 일반 사용자 전용 자신의 예약 개별 조회를 사용할 수 없다`() {
        mockMvc.get("/api/reservation/me")
            .andExpect { status { isForbidden() } }
    }
}