package kr.codesquad.kiosk.orders.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import kr.codesquad.kiosk.exception.BusinessException;
import kr.codesquad.kiosk.exception.ErrorCode;
import kr.codesquad.kiosk.orders.controller.dto.request.OrderReceiptRequest;
import kr.codesquad.kiosk.orders.controller.dto.request.OrdersRequest;
import kr.codesquad.kiosk.orders.controller.dto.response.OrdersIdResponse;
import kr.codesquad.kiosk.orders.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderTestApiController.class)
class OrderTestApiControllerTest {
	private static final FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(Boolean.TRUE)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

	@Autowired
	MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	OrderService orderService;

	@DisplayName("결제 요청(주문)이 들어오면 주문 정보를 등록한 후 주문 id를 반환한다.")
	@Test
	void whenCreateOrder_thenResponse200OK() throws Exception {
		// given
		OrderReceiptRequest orderReceiptRequest = sut.giveMeBuilder(OrderReceiptRequest.class)
				.set("orders", new OrdersRequest(1, 1))
				.sample();
		OrdersIdResponse ordersIdResponse = sut.giveMeOne(OrdersIdResponse.class);
		given(orderService.createOrderWithNonDelayAndAlwaysSucceed(orderReceiptRequest)).willReturn(ordersIdResponse);

		// when & then
		mockMvc.perform(
						post("/test-api/orders-success")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(orderReceiptRequest))
								.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ordersId").value(ordersIdResponse.ordersId()));
	}

	@DisplayName("결제 요청(주문) 실패시 503 에러를 반환한다.")
	@Test
	void whenCreateOrderFail_thenResponse503ServiceUnavailable() throws Exception {
		OrderReceiptRequest orderReceiptRequest = sut.giveMeBuilder(OrderReceiptRequest.class)
				.set("orders", new OrdersRequest(1, 1))
				.sample();
		when(orderService.createOrderWithNonDelayAndAlwaysFail(orderReceiptRequest))
				.thenThrow(new BusinessException(ErrorCode.NETWORK_FAIN_ERROR));

		mockMvc.perform(
						post("/test-api/orders-failure")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(orderReceiptRequest))
								.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is5xxServerError())
				.andExpect(jsonPath("$.message").exists());
	}

}
