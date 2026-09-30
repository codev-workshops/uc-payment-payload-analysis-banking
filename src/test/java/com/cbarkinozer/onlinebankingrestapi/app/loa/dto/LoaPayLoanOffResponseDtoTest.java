package com.cbarkinozer.onlinebankingrestapi.app.loa.dto;

import com.cbarkinozer.onlinebankingrestapi.app.loa.enums.LoaLoanStatusType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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

class LoaPayLoanOffResponseDtoTest {

    private static <T extends LoaPayLoanOffResponseDto> T populate(T dto) {
        dto.setId(7L);
        dto.setCustomerId(42L);
        dto.setPaidAmount(new BigDecimal("9217.97"));
        dto.setRemainingAmount(BigDecimal.ZERO);
        dto.setLoanStatusType(LoaLoanStatusType.PAID);
        return dto;
    }

    private static LoaPayLoanOffResponseDto createDto() {
        return populate(new LoaPayLoanOffResponseDto());
    }

    static Stream<Arguments> singleFieldChanges() {
        return Stream.of(
                change("id differs", dto -> dto.setId(8L)),
                change("id null", dto -> dto.setId(null)),
                change("customerId differs", dto -> dto.setCustomerId(43L)),
                change("customerId null", dto -> dto.setCustomerId(null)),
                change("paidAmount differs", dto -> dto.setPaidAmount(BigDecimal.ONE)),
                change("paidAmount null", dto -> dto.setPaidAmount(null)),
                change("remainingAmount differs", dto -> dto.setRemainingAmount(BigDecimal.ONE)),
                change("remainingAmount null", dto -> dto.setRemainingAmount(null)),
                change("loanStatusType differs", dto -> dto.setLoanStatusType(LoaLoanStatusType.CONTINUING)),
                change("loanStatusType null", dto -> dto.setLoanStatusType(null)));
    }

    private static Arguments change(String description, Consumer<LoaPayLoanOffResponseDto> modification) {
        return Arguments.of(description, modification);
    }

    @Test
    void shouldReturnValuesAssignedThroughSetters() {
        LoaPayLoanOffResponseDto dto = createDto();

        assertAll(
                () -> assertEquals(7L, dto.getId()),
                () -> assertEquals(42L, dto.getCustomerId()),
                () -> assertEquals(new BigDecimal("9217.97"), dto.getPaidAmount()),
                () -> assertEquals(BigDecimal.ZERO, dto.getRemainingAmount()),
                () -> assertEquals(LoaLoanStatusType.PAID, dto.getLoanStatusType()));
    }

    @Test
    void shouldHaveNullFieldsWhenCreatedWithNoArgsConstructor() {
        LoaPayLoanOffResponseDto dto = new LoaPayLoanOffResponseDto();

        assertAll(
                () -> assertNull(dto.getId()),
                () -> assertNull(dto.getCustomerId()),
                () -> assertNull(dto.getPaidAmount()),
                () -> assertNull(dto.getRemainingAmount()),
                () -> assertNull(dto.getLoanStatusType()));
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsMatch() {
        LoaPayLoanOffResponseDto dto = createDto();

        assertEquals(dto, dto);
        assertEqualContract(dto, createDto());
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsAreNull() {
        assertEqualContract(new LoaPayLoanOffResponseDto(), new LoaPayLoanOffResponseDto());
    }

    @Test
    void shouldNotBeEqualToNullOrOtherTypes() {
        LoaPayLoanOffResponseDto dto = createDto();

        assertFalse(dto.equals(null));
        assertFalse(dto.equals("LoaPayLoanOffResponseDto"));
        assertFalse(dto.equals(new LoaLoanDto()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("singleFieldChanges")
    void shouldNotBeEqualWhenSingleFieldDiffers(String description, Consumer<LoaPayLoanOffResponseDto> modification) {
        LoaPayLoanOffResponseDto changed = createDto();
        modification.accept(changed);

        assertNotEqualBothWays(createDto(), changed);
    }

    @Test
    void shouldNotBeEqualToSubclassThatRestrictsCanEqual() {
        LoaPayLoanOffResponseDto restricted = populate(new RestrictedLoaPayLoanOffResponseDto());

        assertFalse(createDto().equals(restricted));
    }

    @Test
    void shouldRenderAllFieldsInToString() {
        assertEquals(
                "LoaPayLoanOffResponseDto(id=7, customerId=42, paidAmount=9217.97, remainingAmount=0, "
                        + "loanStatusType=PAID)",
                createDto().toString());
    }

    @Test
    void shouldSerializeStatusAsEnumName() {
        JsonNode json = OBJECT_MAPPER.valueToTree(createDto());

        assertAll(
                () -> assertEquals(5, json.size()),
                () -> assertEquals("PAID", json.get("loanStatusType").asText()),
                () -> assertEquals(new BigDecimal("9217.97"), json.get("paidAmount").decimalValue()));
    }

    @Test
    void shouldSurviveJsonRoundTrip() throws JsonProcessingException {
        LoaPayLoanOffResponseDto dto = createDto();

        assertEquals(dto, jsonRoundTrip(dto, LoaPayLoanOffResponseDto.class));
    }

    private static class RestrictedLoaPayLoanOffResponseDto extends LoaPayLoanOffResponseDto {

        @Override
        protected boolean canEqual(Object other) {
            return other instanceof RestrictedLoaPayLoanOffResponseDto;
        }
    }
}
