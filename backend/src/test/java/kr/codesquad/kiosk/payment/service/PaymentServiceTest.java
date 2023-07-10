package kr.codesquad.kiosk.payment.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import kr.codesquad.kiosk.exception.BusinessException;
import kr.codesquad.kiosk.exception.ErrorCode;
import kr.codesquad.kiosk.payment.domain.Payment;
import kr.codesquad.kiosk.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
	private static final FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(Boolean.TRUE)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

	@Mock
	private PaymentRepository paymentRepository;

	@InjectMocks
	private PaymentService paymentService;

	@DisplayName("결제 방식 목록을 조회할 수 있다.")
	@Test
	void whenGetPayments_thenReturnPayments() {
		// given
		given(paymentRepository.findAll()).willReturn(sut.giveMe(Payment.class, 3));

		// when
		paymentService.getPayments();

		// then
		then(paymentRepository).should(times(1)).findAll();
	}

	@DisplayName("결제 방식 목록을 조회했을 때 목록이 없으면 PAYMENTS_NOT_FOUND 에러가 발생한다.")
	@Test
	void whenGetPayments_thenThrowsBusinessException() {
		// given
		given(paymentRepository.findAll()).willReturn(List.of());

		// when & then
		assertAll(
				() -> assertThatThrownBy(() -> paymentService.getPayments())
						.isInstanceOf(BusinessException.class)
						.extracting("errorCode").isEqualTo(ErrorCode.PAYMENTS_NOT_FOUND),
				() -> then(paymentRepository).should(times(1)).findAll()
		);
	}
}
