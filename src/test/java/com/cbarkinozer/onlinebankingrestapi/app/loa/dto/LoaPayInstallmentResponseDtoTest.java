package com.cbarkinozer.onlinebankingrestapi.app.loa.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
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

class LoaPayInstallmentResponseDtoTest {

    private static final LocalDate PAYMENT_DATE = LocalDate.of(2026, 9, 30);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 10, 30);

    private static <T extends LoaPayInstallmentResponseDto> T populate(T dto) {
        dto.setLoanId(7L);
        dto.setPaymentAmount(new BigDecimal("961.03"));
        dto.setPaymentDate(PAYMENT_DATE);
        dto.setRemainingPrincipal(new BigDecimal("9217.97"));
        dto.setDueDate(DUE_DATE);
        return dto;
    }

    private static LoaPayInstallmentResponseDto createDto() {
        return populate(new LoaPayInstallmentResponseDto());
    }

    static Stream<Arguments> singleFieldChanges() {
        return Stream.of(
                change("loanId differs", dto -> dto.setLoanId(8L)),
                change("loanId null", dto -> dto.setLoanId(null)),
                change("paymentAmount differs", dto -> dto.setPaymentAmount(BigDecimal.ONE)),
                change("paymentAmount null", dto -> dto.setPaymentAmount(null)),
                change("paymentDate differs", dto -> dto.setPaymentDate(PAYMENT_DATE.plusDays(1))),
                change("paymentDate null", dto -> dto.setPaymentDate(null)),
                change("remainingPrincipal differs", dto -> dto.setRemainingPrincipal(BigDecimal.ONE)),
                change("remainingPrincipal null", dto -> dto.setRemainingPrincipal(null)),
                change("dueDate differs", dto -> dto.setDueDate(DUE_DATE.plusMonths(1))),
                change("dueDate null", dto -> dto.setDueDate(null)));
    }

    private static Arguments change(String description, Consumer<LoaPayInstallmentResponseDto> modification) {
        return Arguments.of(description, modification);
    }

    @Test
    void shouldReturnValuesAssignedThroughSetters() {
        LoaPayInstallmentResponseDto dto = createDto();

        assertAll(
                () -> assertEquals(7L, dto.getLoanId()),
                () -> assertEquals(new BigDecimal("961.03"), dto.getPaymentAmount()),
                () -> assertEquals(PAYMENT_DATE, dto.getPaymentDate()),
                () -> assertEquals(new BigDecimal("9217.97"), dto.getRemainingPrincipal()),
                () -> assertEquals(DUE_DATE, dto.getDueDate()));
    }

    @Test
    void shouldHaveNullFieldsWhenCreatedWithNoArgsConstructor() {
        LoaPayInstallmentResponseDto dto = new LoaPayInstallmentResponseDto();

        assertAll(
                () -> assertNull(dto.getLoanId()),
                () -> assertNull(dto.getPaymentAmount()),
                () -> assertNull(dto.getPaymentDate()),
                () -> assertNull(dto.getRemainingPrincipal()),
                () -> assertNull(dto.getDueDate()));
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsMatch() {
        LoaPayInstallmentResponseDto dto = createDto();

        assertEquals(dto, dto);
        assertEqualContract(dto, createDto());
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsAreNull() {
        assertEqualContract(new LoaPayInstallmentResponseDto(), new LoaPayInstallmentResponseDto());
    }

    @Test
    void shouldNotBeEqualToNullOrOtherTypes() {
        LoaPayInstallmentResponseDto dto = createDto();

        assertFalse(dto.equals(null));
        assertFalse(dto.equals("LoaPayInstallmentResponseDto"));
        assertFalse(dto.equals(new LoaPayLoanOffResponseDto()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("singleFieldChanges")
    void shouldNotBeEqualWhenSingleFieldDiffers(
            String description, Consumer<LoaPayInstallmentResponseDto> modification) {
        LoaPayInstallmentResponseDto changed = createDto();
        modification.accept(changed);

        assertNotEqualBothWays(createDto(), changed);
    }

    @Test
    void shouldNotBeEqualToSubclassThatRestrictsCanEqual() {
        LoaPayInstallmentResponseDto restricted = populate(new RestrictedLoaPayInstallmentResponseDto());

        assertFalse(createDto().equals(restricted));
    }

    @Test
    void shouldRenderAllFieldsInToString() {
        assertEquals(
                "LoaPayInstallmentResponseDto(loanId=7, paymentAmount=961.03, PaymentDate=2026-09-30, "
                        + "remainingPrincipal=9217.97, dueDate=2026-10-30)",
                createDto().toString());
    }

    @Test
    void shouldSerializePaymentDateUnderCamelCasePropertyName() {
        JsonNode json = OBJECT_MAPPER.valueToTree(createDto());

        assertAll(
                () -> assertEquals(5, json.size()),
                () -> assertEquals("2026-09-30", json.get("paymentDate").asText()),
                () -> assertEquals("2026-10-30", json.get("dueDate").asText()),
                () -> assertFalse(json.has("PaymentDate")));
    }

    @Test
    void shouldSurviveJsonRoundTrip() throws JsonProcessingException {
        LoaPayInstallmentResponseDto dto = createDto();

        assertEquals(dto, jsonRoundTrip(dto, LoaPayInstallmentResponseDto.class));
    }

    private static class RestrictedLoaPayInstallmentResponseDto extends LoaPayInstallmentResponseDto {

        @Override
        protected boolean canEqual(Object other) {
            return other instanceof RestrictedLoaPayInstallmentResponseDto;
        }
    }
}
