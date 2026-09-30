package com.cbarkinozer.onlinebankingrestapi.app.loa.dto;

import com.cbarkinozer.onlinebankingrestapi.app.loa.enums.LoaLoanStatusType;
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

class LoaLoanDtoTest {

    private static final LocalDate DUE_DATE = LocalDate.of(2026, 10, 30);

    private static <T extends LoaLoanDto> T populate(T dto) {
        dto.setId(7L);
        dto.setCustomerId(42L);
        dto.setInstallmentCount(12);
        dto.setPrincipalLoanAmount(new BigDecimal("10000.00"));
        dto.setMonthlyInstallmentAmount(new BigDecimal("961.03"));
        dto.setInterestToBePaid(new BigDecimal("179.00"));
        dto.setPrincipalToBePaid(new BigDecimal("782.03"));
        dto.setRemainingPrincipal(new BigDecimal("9217.97"));
        dto.setDueDate(DUE_DATE);
        dto.setLoanStatusType(LoaLoanStatusType.CONTINUING);
        return dto;
    }

    private static LoaLoanDto createDto() {
        return populate(new LoaLoanDto());
    }

    static Stream<Arguments> singleFieldChanges() {
        return Stream.of(
                change("id differs", dto -> dto.setId(8L)),
                change("id null", dto -> dto.setId(null)),
                change("customerId differs", dto -> dto.setCustomerId(43L)),
                change("customerId null", dto -> dto.setCustomerId(null)),
                change("installmentCount differs", dto -> dto.setInstallmentCount(24)),
                change("installmentCount null", dto -> dto.setInstallmentCount(null)),
                change("principalLoanAmount differs", dto -> dto.setPrincipalLoanAmount(BigDecimal.ONE)),
                change("principalLoanAmount null", dto -> dto.setPrincipalLoanAmount(null)),
                change("monthlyInstallmentAmount differs", dto -> dto.setMonthlyInstallmentAmount(BigDecimal.ONE)),
                change("monthlyInstallmentAmount null", dto -> dto.setMonthlyInstallmentAmount(null)),
                change("interestToBePaid differs", dto -> dto.setInterestToBePaid(BigDecimal.ONE)),
                change("interestToBePaid null", dto -> dto.setInterestToBePaid(null)),
                change("principalToBePaid differs", dto -> dto.setPrincipalToBePaid(BigDecimal.ONE)),
                change("principalToBePaid null", dto -> dto.setPrincipalToBePaid(null)),
                change("remainingPrincipal differs", dto -> dto.setRemainingPrincipal(BigDecimal.ONE)),
                change("remainingPrincipal null", dto -> dto.setRemainingPrincipal(null)),
                change("dueDate differs", dto -> dto.setDueDate(DUE_DATE.plusMonths(1))),
                change("dueDate null", dto -> dto.setDueDate(null)),
                change("loanStatusType differs", dto -> dto.setLoanStatusType(LoaLoanStatusType.LATE)),
                change("loanStatusType null", dto -> dto.setLoanStatusType(null)));
    }

    private static Arguments change(String description, Consumer<LoaLoanDto> modification) {
        return Arguments.of(description, modification);
    }

    @Test
    void shouldReturnValuesAssignedThroughSetters() {
        LoaLoanDto dto = createDto();

        assertAll(
                () -> assertEquals(7L, dto.getId()),
                () -> assertEquals(42L, dto.getCustomerId()),
                () -> assertEquals(12, dto.getInstallmentCount()),
                () -> assertEquals(new BigDecimal("10000.00"), dto.getPrincipalLoanAmount()),
                () -> assertEquals(new BigDecimal("961.03"), dto.getMonthlyInstallmentAmount()),
                () -> assertEquals(new BigDecimal("179.00"), dto.getInterestToBePaid()),
                () -> assertEquals(new BigDecimal("782.03"), dto.getPrincipalToBePaid()),
                () -> assertEquals(new BigDecimal("9217.97"), dto.getRemainingPrincipal()),
                () -> assertEquals(DUE_DATE, dto.getDueDate()),
                () -> assertEquals(LoaLoanStatusType.CONTINUING, dto.getLoanStatusType()));
    }

    @Test
    void shouldHaveNullFieldsWhenCreatedWithNoArgsConstructor() {
        LoaLoanDto dto = new LoaLoanDto();

        assertAll(
                () -> assertNull(dto.getId()),
                () -> assertNull(dto.getCustomerId()),
                () -> assertNull(dto.getInstallmentCount()),
                () -> assertNull(dto.getPrincipalLoanAmount()),
                () -> assertNull(dto.getMonthlyInstallmentAmount()),
                () -> assertNull(dto.getInterestToBePaid()),
                () -> assertNull(dto.getPrincipalToBePaid()),
                () -> assertNull(dto.getRemainingPrincipal()),
                () -> assertNull(dto.getDueDate()),
                () -> assertNull(dto.getLoanStatusType()));
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsMatch() {
        LoaLoanDto dto = createDto();

        assertEquals(dto, dto);
        assertEqualContract(dto, createDto());
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsAreNull() {
        assertEqualContract(new LoaLoanDto(), new LoaLoanDto());
    }

    @Test
    void shouldNotBeEqualToNullOrOtherTypes() {
        LoaLoanDto dto = createDto();

        assertFalse(dto.equals(null));
        assertFalse(dto.equals("LoaLoanDto"));
        assertFalse(dto.equals(new LoaPayLoanOffResponseDto()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("singleFieldChanges")
    void shouldNotBeEqualWhenSingleFieldDiffers(String description, Consumer<LoaLoanDto> modification) {
        LoaLoanDto changed = createDto();
        modification.accept(changed);

        assertNotEqualBothWays(createDto(), changed);
    }

    @Test
    void shouldNotBeEqualToSubclassThatRestrictsCanEqual() {
        LoaLoanDto restricted = populate(new RestrictedLoaLoanDto());

        assertFalse(createDto().equals(restricted));
    }

    @Test
    void shouldRenderAllFieldsInToString() {
        assertEquals(
                "LoaLoanDto(id=7, customerId=42, installmentCount=12, principalLoanAmount=10000.00, "
                        + "monthlyInstallmentAmount=961.03, interestToBePaid=179.00, principalToBePaid=782.03, "
                        + "remainingPrincipal=9217.97, dueDate=2026-10-30, loanStatusType=CONTINUING)",
                createDto().toString());
    }

    @Test
    void shouldSerializeDueDateAsIsoStringAndStatusAsEnumName() {
        JsonNode json = OBJECT_MAPPER.valueToTree(createDto());

        assertAll(
                () -> assertEquals(10, json.size()),
                () -> assertEquals("2026-10-30", json.get("dueDate").asText()),
                () -> assertEquals("CONTINUING", json.get("loanStatusType").asText()),
                () -> assertEquals(42L, json.get("customerId").longValue()));
    }

    @Test
    void shouldSurviveJsonRoundTrip() throws JsonProcessingException {
        LoaLoanDto dto = createDto();

        assertEquals(dto, jsonRoundTrip(dto, LoaLoanDto.class));
    }

    private static class RestrictedLoaLoanDto extends LoaLoanDto {

        @Override
        protected boolean canEqual(Object other) {
            return other instanceof RestrictedLoaLoanDto;
        }
    }
}
