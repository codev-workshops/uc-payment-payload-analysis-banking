package com.cbarkinozer.onlinebankingrestapi.app.loa.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.cbarkinozer.onlinebankingrestapi.app.loa.dto.DtoTestSupport.OBJECT_MAPPER;
import static com.cbarkinozer.onlinebankingrestapi.app.loa.dto.DtoTestSupport.assertEqualContract;
import static com.cbarkinozer.onlinebankingrestapi.app.loa.dto.DtoTestSupport.assertNotEqualBothWays;
import static com.cbarkinozer.onlinebankingrestapi.app.loa.dto.DtoTestSupport.jsonRoundTrip;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoaCalculateLateFeeResponseDtoTest {

    private static <T extends LoaCalculateLateFeeResponseDto> T populate(T dto) {
        dto.setTotalLateFee(new BigDecimal("45.60"));
        dto.setLateFeeRate(new BigDecimal("0.0152"));
        dto.setLateInterestTax(new BigDecimal("6.84"));
        dto.setLateDayCount(3L);
        return dto;
    }

    private static LoaCalculateLateFeeResponseDto createDto() {
        return populate(new LoaCalculateLateFeeResponseDto());
    }

    static Stream<Arguments> singleFieldChanges() {
        return Stream.of(
                change("totalLateFee differs", dto -> dto.setTotalLateFee(BigDecimal.ONE)),
                change("totalLateFee null", dto -> dto.setTotalLateFee(null)),
                change("lateFeeRate differs", dto -> dto.setLateFeeRate(BigDecimal.ONE)),
                change("lateFeeRate null", dto -> dto.setLateFeeRate(null)),
                change("lateInterestTax differs", dto -> dto.setLateInterestTax(BigDecimal.ONE)),
                change("lateInterestTax null", dto -> dto.setLateInterestTax(null)),
                change("lateDayCount differs", dto -> dto.setLateDayCount(4L)),
                change("lateDayCount null", dto -> dto.setLateDayCount(null)));
    }

    private static Arguments change(String description, Consumer<LoaCalculateLateFeeResponseDto> modification) {
        return Arguments.of(description, modification);
    }

    @Test
    void shouldReturnValuesAssignedThroughSetters() {
        LoaCalculateLateFeeResponseDto dto = createDto();

        assertAll(
                () -> assertEquals(new BigDecimal("45.60"), dto.getTotalLateFee()),
                () -> assertEquals(new BigDecimal("0.0152"), dto.getLateFeeRate()),
                () -> assertEquals(new BigDecimal("6.84"), dto.getLateInterestTax()),
                () -> assertEquals(3L, dto.getLateDayCount()));
    }

    @Test
    void shouldHaveNullFieldsWhenCreatedWithNoArgsConstructor() {
        LoaCalculateLateFeeResponseDto dto = new LoaCalculateLateFeeResponseDto();

        assertAll(
                () -> assertNull(dto.getTotalLateFee()),
                () -> assertNull(dto.getLateFeeRate()),
                () -> assertNull(dto.getLateInterestTax()),
                () -> assertNull(dto.getLateDayCount()));
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsMatch() {
        LoaCalculateLateFeeResponseDto dto = createDto();

        assertEquals(dto, dto);
        assertEqualContract(dto, createDto());
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsAreNull() {
        assertEqualContract(new LoaCalculateLateFeeResponseDto(), new LoaCalculateLateFeeResponseDto());
    }

    @Test
    void shouldNotBeEqualToNullOrOtherTypes() {
        LoaCalculateLateFeeResponseDto dto = createDto();

        assertFalse(dto.equals(null));
        assertFalse(dto.equals("LoaCalculateLateFeeResponseDto"));
        assertFalse(dto.equals(new LoaCalculateLoanResponseDto()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("singleFieldChanges")
    void shouldNotBeEqualWhenSingleFieldDiffers(
            String description, Consumer<LoaCalculateLateFeeResponseDto> modification) {
        LoaCalculateLateFeeResponseDto changed = createDto();
        modification.accept(changed);

        assertNotEqualBothWays(createDto(), changed);
    }

    @Test
    void shouldNotBeEqualToSubclassThatRestrictsCanEqual() {
        LoaCalculateLateFeeResponseDto restricted = populate(new RestrictedLoaCalculateLateFeeResponseDto());

        assertFalse(createDto().equals(restricted));
    }

    @Test
    void shouldRenderAllFieldsInToString() {
        assertEquals(
                "LoaCalculateLateFeeResponseDto(totalLateFee=45.60, lateFeeRate=0.0152, "
                        + "lateInterestTax=6.84, lateDayCount=3)",
                createDto().toString());
    }

    @Test
    void shouldSerializeWithCamelCasePropertyNamesAndPreserveAmountScale() throws JsonProcessingException {
        assertEquals(
                "{\"totalLateFee\":45.60,\"lateFeeRate\":0.0152,\"lateInterestTax\":6.84,\"lateDayCount\":3}",
                OBJECT_MAPPER.writeValueAsString(createDto()));
    }

    @Test
    void shouldSurviveJsonRoundTrip() throws JsonProcessingException {
        LoaCalculateLateFeeResponseDto dto = createDto();

        assertEquals(dto, jsonRoundTrip(dto, LoaCalculateLateFeeResponseDto.class));
    }

    private static class RestrictedLoaCalculateLateFeeResponseDto extends LoaCalculateLateFeeResponseDto {

        @Override
        protected boolean canEqual(Object other) {
            return other instanceof RestrictedLoaCalculateLateFeeResponseDto;
        }
    }
}
