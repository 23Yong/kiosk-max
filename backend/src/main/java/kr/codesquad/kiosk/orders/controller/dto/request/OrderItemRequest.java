package kr.codesquad.kiosk.orders.controller.dto.request;

import java.util.List;

import kr.codesquad.kiosk.orderitem.domain.OrderItem;
import kr.codesquad.kiosk.orderitem.domain.OrderItemOption;

public record OrderItemRequest(
	Integer itemId,
	Integer count,
	List<Integer> options
) {
	public OrderItem toOrderItem() {
		return OrderItem.builder()
			.itemId(itemId)
			.itemQuantity(count)
			.orderItemOptions(
				options.stream()
					.map(
						i -> OrderItemOption.builder().optionsId(i).build()
					)
					.toList()
			)
			.build();
	}
}
