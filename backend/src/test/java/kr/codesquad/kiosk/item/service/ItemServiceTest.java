package kr.codesquad.kiosk.item.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import kr.codesquad.kiosk.exception.BusinessException;
import kr.codesquad.kiosk.exception.ErrorCode;
import kr.codesquad.kiosk.item.controller.dto.response.ItemDetailsResponse;
import kr.codesquad.kiosk.item.controller.dto.response.OptionsResponse;
import kr.codesquad.kiosk.item.domain.Item;
import kr.codesquad.kiosk.item.domain.Options;
import kr.codesquad.kiosk.item.repository.ItemRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	private static final FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private ItemService itemService;

	@DisplayName("아이템 아이디로 아이템 상세 항목을 조회할 수 있다.")
	@Test
	void givenItemId_whenGetItemDetails_thenReturnsItemDetailsResponse() {
		// given
		int itemId = 1;
		Item item = sut.giveMeOne(Item.class);
		Map<String, List<Options>> optionsMap = createOptionsMap();
		Map<String, List<OptionsResponse>> options = createOptions();

		given(itemRepository.findById(anyInt())).willReturn(Optional.of(item));
		given(itemRepository.findOptionsByItemId(anyInt())).willReturn(optionsMap);

		// when
		ItemDetailsResponse itemDetails = itemService.getItemDetails(itemId);

		/// then
		ItemDetailsResponse response = ItemDetailsResponse.from(item, options);

		SoftAssertions.assertSoftly(softAssertions -> {
			softAssertions.assertThat(itemDetails.id()).isEqualTo(response.id());
			softAssertions.assertThat(itemDetails.name()).isEqualTo(response.name());
			softAssertions.assertThat(itemDetails.price()).isEqualTo(response.price());
			softAssertions.assertThat(itemDetails.options()).isEqualTo(response.options());
		});
	}

	@DisplayName("존재하지 않는 아이템 아이디로 아이템 상세 항목을 조회하면 ITEM_NOT_FOUND 에러가 발생한다다.")
	@Test
	void givenWrongItemId_whenGetItemDetails_thenThrowsBusinessException() {
		// given
		int itemId = 999999;
		given(itemRepository.findById(anyInt())).willReturn(Optional.empty());

		// when & then
		assertAll(
				() -> assertThatThrownBy(() -> itemService.getItemDetails(itemId))
						.isInstanceOf(BusinessException.class)
						.extracting("errorCode").isEqualTo(ErrorCode.ITEM_NOT_FOUND),
				() -> then(itemRepository).should().findById(anyInt()),
				() -> then(itemRepository).should(times(0)).findOptionsByItemId(anyInt())
		);
	}

	private Map<String, List<OptionsResponse>> createOptions() {
		Map<String, List<OptionsResponse>> map = new HashMap<>();

		map.put("Size",
				List.of(
						new OptionsResponse(1, "Small"),
						new OptionsResponse(2, "Medium"),
						new OptionsResponse(3, "Large")
				)
		);
		map.put("Temperature", List.of(new OptionsResponse(5, "Ice")));

		return map;
	}

	private Map<String, List<Options>> createOptionsMap() {
		return Map.of("Size", List.of(new Options(1, "Small"), new Options(2, "Medium"), new Options(3, "Large")),
				"Temperature", List.of(new Options(5, "Ice")));
	}
}
