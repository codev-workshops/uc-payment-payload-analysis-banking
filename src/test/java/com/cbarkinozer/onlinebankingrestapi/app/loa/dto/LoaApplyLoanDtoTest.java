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

class LoaApplyLoanDtoTest {

    private static <T extends LoaApplyLoanDto> T populate(T dto) {
        dto.setInstallmentCount(12);
        dto.setPrincipalLoanAmount(new BigDecimal("10000.00"));
        dto.setMonthlySalary(new BigDecimal("5000.00"));
        return dto;
    }

    private static LoaApplyLoanDto createDto() {
        return populate(new LoaApplyLoanDto());
    }

    static Stream<Arguments> singleFieldChanges() {
        return Stream.of(
                change("installmentCount differs", dto -> dto.setInstallmentCount(24)),
                change("installmentCount null", dto -> dto.setInstallmentCount(null)),
                change("principalLoanAmount differs", dto -> dto.setPrincipalLoanAmount(BigDecimal.ONE)),
                change("principalLoanAmount null", dto -> dto.setPrincipalLoanAmount(null)),
                change("monthlySalary differs", dto -> dto.setMonthlySalary(BigDecimal.TEN)),
                change("monthlySalary null", dto -> dto.setMonthlySalary(null)));
    }

    private static Arguments change(String description, Consumer<LoaApplyLoanDto> modification) {
        return Arguments.of(description, modification);
    }

    @Test
    void shouldReturnValuesAssignedThroughSetters() {
        LoaApplyLoanDto dto = createDto();

        assertAll(
                () -> assertEquals(12, dto.getInstallmentCount()),
                () -> assertEquals(new BigDecimal("10000.00"), dto.getPrincipalLoanAmount()),
                () -> assertEquals(new BigDecimal("5000.00"), dto.getMonthlySalary()));
    }

    @Test
    void shouldHaveNullFieldsWhenCreatedWithNoArgsConstructor() {
        LoaApplyLoanDto dto = new LoaApplyLoanDto();

        assertAll(
                () -> assertNull(dto.getInstallmentCount()),
                () -> assertNull(dto.getPrincipalLoanAmount()),
                () -> assertNull(dto.getMonthlySalary()));
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsMatch() {
        LoaApplyLoanDto dto = createDto();

        assertEquals(dto, dto);
        assertEqualContract(dto, createDto());
    }

    @Test
    void shouldBeEqualWithSameHashCodeWhenAllFieldsAreNull() {
        assertEqualContract(new LoaApplyLoanDto(), new LoaApplyLoanDto());
    }

    @Test
    void shouldNotBeEqualToNullOrOtherTypes() {
        LoaApplyLoanDto dto = createDto();

        assertFalse(dto.equals(null));
        assertFalse(dto.equals("LoaApplyLoanDto"));
        assertFalse(dto.equals(new LoaLoanDto()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("singleFieldChanges")
    void shouldNotBeEqualWhenSingleFieldDiffers(String description, Consumer<LoaApplyLoanDto> modification) {
        LoaApplyLoanDto changed = createDto();
        modification.accept(changed);

        assertNotEqualBothWays(createDto(), changed);
    }

    @Test
    void shouldNotBeEqualToSubclassThatRestrictsCanEqual() {
        LoaApplyLoanDto restricted = populate(new RestrictedLoaApplyLoanDto());

        assertFalse(createDto().equals(restricted));
    }

    @Test
    void shouldRenderAllFieldsInToString() {
        assertEquals(
                "LoaApplyLoanDto(installmentCount=12, principalLoanAmount=10000.00, monthlySalary=5000.00)",
                createDto().toString());
    }

    @Test
    void shouldDeserializeLoanApplicationRequestBody() throws JsonProcessingException {
        String requestBody = "{\"installmentCount\":12,\"principalLoanAmount\":10000.00,\"monthlySalary\":5000.00}";

        LoaApplyLoanDto dto = OBJECT_MAPPER.readValue(requestBody, LoaApplyLoanDto.class);

        assertEquals(createDto(), dto);
    }

    @Test
    void shouldSurviveJsonRoundTrip() throws JsonProcessingException {
        LoaApplyLoanDto dto = createDto();

        assertEquals(dto, jsonRoundTrip(dto, LoaApplyLoanDto.class));
    }

    private static class RestrictedLoaApplyLoanDto extends LoaApplyLoanDto {

        @Override
        protected boolean canEqual(Object other) {
            return other instanceof RestrictedLoaApplyLoanDto;
        }
    }
}
