package io.github.jeongyounghyeon.roe.presentation.order

import com.jayway.jsonpath.JsonPath
import io.github.jeongyounghyeon.roe.domain.order.OrderEvent
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.PAYMENT_PROCESSING
import io.github.jeongyounghyeon.roe.domain.order.OrderStatus.PENDING_PAYMENT
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.UUID

@SpringBootTest(webEnvironment = MOCK)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    private fun createOrderAndGetId(): String {
        val result = mockMvc.perform(post("/orders"))
            .andExpect(status().isCreated)
            .andReturn()
        return JsonPath.read(result.response.contentAsString, "$.id")
    }

    @Test
    fun `주문 생성 시 201 반환`() {
        mockMvc.perform(post("/orders"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.status").value(PENDING_PAYMENT.name))
    }

    @Test
    fun `유효한 이벤트 처리 시 200 반환`() {
        val orderId = createOrderAndGetId()

        mockMvc.perform(
            post("/orders/$orderId/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"event":"${OrderEvent.REQUEST_PAY.name}"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(PAYMENT_PROCESSING.name))
    }

    @Test
    fun `주문 ID로 조회 시 200 반환`() {
        val orderId = createOrderAndGetId()

        mockMvc.perform(get("/orders/$orderId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(orderId))
            .andExpect(jsonPath("$.histories").isNotEmpty)
    }

    @Test
    fun `존재하지 않는 주문 조회 시 404 반환`() {
        mockMvc.perform(get("/orders/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `유효하지 않은 전이 시도 시 409 반환`() {
        val orderId = createOrderAndGetId()

        // PENDING_PAYMENT → PAYMENT_PROCESSING
        mockMvc.perform(
            post("/orders/$orderId/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"event":"${OrderEvent.REQUEST_PAY.name}"}""")
        )

        // PAYMENT_PROCESSING 에서 CANCEL_REQUEST 는 불가
        mockMvc.perform(
            post("/orders/$orderId/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"event":"${OrderEvent.CANCEL_REQUEST.name}"}""")
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `존재하지 않는 주문에 이벤트 처리 시 404 반환`() {
        mockMvc.perform(
            post("/orders/${UUID.randomUUID()}/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"event":"${OrderEvent.REQUEST_PAY.name}"}""")
        )
            .andExpect(status().isNotFound)
    }
}
