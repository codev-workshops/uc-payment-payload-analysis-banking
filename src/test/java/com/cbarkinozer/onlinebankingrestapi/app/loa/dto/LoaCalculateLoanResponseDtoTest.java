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

class LoaCalculateLoanResponseDtoTest {

    private static <T extends LoaCalculateLoanResponseDto> T populate(T dto) {
        dto.setInterestRate(new BigDecimal("0.0179"));
        dto.setTotalInterest(new BigDecimal("1532.40"));
        dto.setMonthlyInstallmentAmount(new BigDecimal("961.03"));
        dto.setTotalPayment(new BigDecimal("11532.40"));
        dto.setAnnualCostRate(new BigDecimal("0.2148"));
        dto.setAllocationFee(new BigDecimal("50.00"));
        return dto;
    }

    private static LoaCalculateLoanResponseDto createDto() {
        return populate(new LoaCalculateLoanResponseDto());
    }

    static Stream<Arguments> singleFieldChanges() {
        return Stream.of(
                change("interestRate differs", dto -> dto.setInterestRate(BigDecimal.ONE)),
                change("interestRate null", dto -> dto.setInterestRate(null)),
                change("totalInterest differs", dto -> dto.setTotalInterest(BigDecimal.ONE)),
                change("totalInterest null", dto -> dto.setTotalInterest(null)),
                change("monthlyInstallmentAmount differs", dto -> dto.setMonthlyInstallmentAmount(BigDecimal.ONE)),
                change("monthlyInstallmentAmount null", dto -> dto.setMonthlyInstallmentAmount(null)),
                change("totalPayment differs", dto -> dto.setTotalPayment(BigDecimal.ONE)),
                change("totalPayment null", dto -> dto.setTotalPayment(null)),
                change("annualCostRate differs", dto -> dto.setAnnualCostRate(BigDecimal.ONE)),
                change("annualCostRate null", dto -> dto.setAnnualCostRate(null)),
                change("allocationFee differs", dto -> dto.setAllocationFee(BigDecimal.ONE)),
                change("allocationFee null", dto -> dto.setAllocationFee(null)));
    }

    private static Arguments change(String description, Consumer<LoaCalculateLoanResponseDto> modification) {
        return Arguments.of(description, modification);
    }

    @Test
    void shouldReturnValuesAssignedThroughSetters() {
        LoaCalculateLoanResponseDto dto = createDto();

        assertAll(
                () -> assertEquals(new BigDecimal("0.0179"), dto.getInterestRate()),
                () -> assertEquals(new BigDecimal("1532.40"), dto.getTotalInterest()),
                () -> assertEquals(new BigDecimal("961.03"), dto.getMonthlyInstallmentAmount()),
                () -> assertEquals(new BigDecimal("11532.40"), dto.getTotalPayment()),
                () -> assertEquals(new BigDecimal("0.2148"), dto.getAnnualCostRate()),
                () -> assertEquals(new BigDecimal("50.00"), dto.getAllocationFee()));
    }

    @Test
    void shouldHaveNullFieldsWhenCreatedWithNoArgsConstructor() {
        LoaCalculateLoanResponseDto dto = new LoaCalculateLoanResponseDto();

        assertAll(
                () -> assertNull(dto.getInterestRate()),
                () -> assertNull(dto.getTotalInterest()),
                () -> assertNull(dto.getMonthlyInstallmentAmount()),
                () -> assertNull(dto.getTotalPayment()),
                () -> assertNull(dto.getAnnualCostRate()),
                () -> assertNull(dto.getAllocationFee()));
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsMatch() {
        LoaCalculateLoanResponseDto dto = createDto();

        assertEquals(dto, dto);
        assertEqualContract(dto, createDto());
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsAreNull() {
        assertEqualContract(new LoaCalculateLoanResponseDto(), new LoaCalculateLoanResponseDto());
    }

    @Test
    void shouldNotBeEqualToNullOrOtherTypes() {
        LoaCalculateLoanResponseDto dto = createDto();

        assertFalse(dto.equals(null));
        assertFalse(dto.equals("LoaCalculateLoanResponseDto"));
        assertFalse(dto.equals(new LoaCalculateLateFeeResponseDto()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("singleFieldChanges")
    void shouldNotBeEqualWhenSingleFieldDiffers(String description, Consumer<LoaCalculateLoanResponseDto> modification) {
        LoaCalculateLoanResponseDto changed = createDto();
        modification.accept(changed);

        assertNotEqualBothWays(createDto(), changed);
    }

    @Test
    void shouldNotBeEqualToSubclassThatRestrictsCanEqual() {
        LoaCalculateLoanResponseDto restricted = populate(new RestrictedLoaCalculateLoanResponseDto());

        assertFalse(createDto().equals(restricted));
    }

    @Test
    void shouldRenderAllFieldsInToString() {
        assertEquals(
                "LoaCalculateLoanResponseDto(interestRate=0.0179, totalInterest=1532.40, "
                        + "monthlyInstallmentAmount=961.03, totalPayment=11532.40, annualCostRate=0.2148, "
                        + "allocationFee=50.00)",
                createDto().toString());
    }

    @Test
    void shouldSerializeWithCamelCasePropertyNamesAndPreserveAmountScale() throws JsonProcessingException {
        assertEquals(
                "{\"interestRate\":0.0179,\"totalInterest\":1532.40,\"monthlyInstallmentAmount\":961.03,"
                        + "\"totalPayment\":11532.40,\"annualCostRate\":0.2148,\"allocationFee\":50.00}",
                OBJECT_MAPPER.writeValueAsString(createDto()));
    }

    @Test
    void shouldSurviveJsonRoundTrip() throws JsonProcessingException {
        LoaCalculateLoanResponseDto dto = createDto();

        assertEquals(dto, jsonRoundTrip(dto, LoaCalculateLoanResponseDto.class));
    }

    private static class RestrictedLoaCalculateLoanResponseDto extends LoaCalculateLoanResponseDto {

        @Override
        protected boolean canEqual(Object other) {
            return other instanceof RestrictedLoaCalculateLoanResponseDto;
        }
    }
}
