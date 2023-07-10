package kr.codesquad.kiosk.orders.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import kr.codesquad.kiosk.exception.BusinessException;
import kr.codesquad.kiosk.exception.ErrorCode;
import kr.codesquad.kiosk.orders.controller.dto.OrderItemResponse;
import kr.codesquad.kiosk.orders.controller.dto.OrdersResponse;
import kr.codesquad.kiosk.orders.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
	private static final FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(Boolean.TRUE)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private OrderService orderService;

	@DisplayName("영수증 정보를 얻을 때 주문번호가 주어지면 영수증 정보를 얻는데 성공한다.")
	@Test
	void givenOrderId_whenGetReceipt_thenReturnsOrderReceipt() {
		// given
		int orderId = 1;

		given(orderRepository.findOrdersResponseByOrderId(orderId))
				.willReturn(Optional.of(sut.giveMeOne(OrdersResponse.class)));
		given(orderRepository.findOrderItemResponsesByOrderId(orderId))
				.willReturn(List.of(sut.giveMeOne(OrderItemResponse.class)));

		// when
		orderService.getReceipt(orderId);

		// then
		assertAll(
				() -> then(orderRepository).should(times(1)).findOrdersResponseByOrderId(anyInt()),
				() -> then(orderRepository).should(times(1)).findOrderItemResponsesByOrderId(anyInt())
		);
	}

	@DisplayName("영수증 정보를 얻을 때 잘못된 주문번호가 주어지면 예외를 던진다.")
	@Test
	void givenWrongOrderId_whenGetReceipt_thenThrowsException() {
		// given
		given(orderRepository.findOrdersResponseByOrderId(anyInt())).willReturn(Optional.empty());

		// when & then
		assertAll(
				() -> assertThatThrownBy(() -> orderService.getReceipt(9876))
						.isInstanceOf(BusinessException.class)
						.extracting("errorCode")
						.isEqualTo(ErrorCode.ORDER_NOT_FOUND),
				() -> then(orderRepository).should(times(1)).findOrdersResponseByOrderId(anyInt()),
				() -> then(orderRepository).should(times(0)).findOrderItemResponsesByOrderId(anyInt())
		);
	}
}
