package kr.codesquad.kiosk.payment.controller;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import kr.codesquad.kiosk.exception.BusinessException;
import kr.codesquad.kiosk.exception.ErrorCode;
import kr.codesquad.kiosk.payment.controller.response.PaymentResponse;
import kr.codesquad.kiosk.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
class PaymentControllerTest {
	private static final FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(Boolean.TRUE)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

	@Autowired
	MockMvc mockMvc;

	@MockBean
	PaymentService paymentService;

	@DisplayName("결제 방식 목록을 조회할 수 있다.")
	@Test
	void whenGetPayments_then200OK() throws Exception {
		List<PaymentResponse> payments = sut.giveMe(PaymentResponse.class, 3);
		when(paymentService.getPayments()).thenReturn(payments);

		mockMvc.perform(
						get("/api/payments")
				)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.payments").exists());
	}

	@DisplayName("결제 방식 목록이 없으면 404 Not Found를 반환한다.")
	@Test
	void givenNoPayments_whenGetItemDetails_thenResponse404NotFound() throws Exception {
		when(paymentService.getPayments()).thenThrow(new BusinessException(ErrorCode.PAYMENTS_NOT_FOUND));

		mockMvc.perform(
						get("/api/payments")
				)
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value(ErrorCode.PAYMENTS_NOT_FOUND.getDescription()));
	}
}
