package com.cbarkinozer.onlinebankingrestapi.app.loa.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.cbarkinozer.onlinebankingrestapi.app.gen.entity.BaseAdditionalFields;
import com.cbarkinozer.onlinebankingrestapi.app.loa.dto.LoaApplyLoanDto;
import com.cbarkinozer.onlinebankingrestapi.app.loa.dto.LoaLoanDto;
import com.cbarkinozer.onlinebankingrestapi.app.loa.dto.LoaPayLoanOffResponseDto;
import com.cbarkinozer.onlinebankingrestapi.app.loa.entity.LoaLoan;
import com.cbarkinozer.onlinebankingrestapi.app.loa.enums.LoaLoanStatusType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LoaLoanMapperTest {

  private final LoaLoanMapper mapper = LoaLoanMapper.INSTANCE;

  @Test
  void instanceIsGeneratedImplementationSingleton() {
    assertNotNull(LoaLoanMapper.INSTANCE);
    assertInstanceOf(LoaLoanMapperImpl.class, LoaLoanMapper.INSTANCE);
    assertSame(LoaLoanMapper.INSTANCE, mapper);
  }

  @Test
  void convertToLoaLoanReturnsNullForNullInput() {
    assertNull(mapper.convertToLoaLoan(null));
  }

  @Test
  void convertToLoaLoanCopiesInstallmentCountAndPrincipalOnly() {
    LoaApplyLoanDto dto = new LoaApplyLoanDto();
    dto.setInstallmentCount(12);
    dto.setPrincipalLoanAmount(new BigDecimal("10000.00"));
    dto.setMonthlySalary(new BigDecimal("5000.00"));

    LoaLoan loan = mapper.convertToLoaLoan(dto);

    assertNotNull(loan);
    assertEquals(12, loan.getInstallmentCount());
    assertEquals(new BigDecimal("10000.00"), loan.getPrincipalLoanAmount());
    assertNull(loan.getId());
    assertNull(loan.getCustomerId());
    assertNull(loan.getMonthlyInstallmentAmount());
    assertNull(loan.getInterestToBePaid());
    assertNull(loan.getPrincipalToBePaid());
    assertNull(loan.getRemainingPrincipal());
    assertNull(loan.getDueDate());
    assertNull(loan.getLoanStatusType());
    assertNull(loan.getBaseAdditionalFields());
  }

  @Test
  void convertToLoaLoanKeepsNullFieldsNull() {
    LoaLoan loan = mapper.convertToLoaLoan(new LoaApplyLoanDto());

    assertNotNull(loan);
    assertNull(loan.getInstallmentCount());
    assertNull(loan.getPrincipalLoanAmount());
  }

  @Test
  void convertToLoaLoanDtoReturnsNullForNullInput() {
    assertNull(mapper.convertToLoaLoanDto(null));
  }

  @Test
  void convertToLoaLoanDtoCopiesAllFields() {
    LoaLoan loan = buildLoan(LoaLoanStatusType.CONTINUING);

    LoaLoanDto dto = mapper.convertToLoaLoanDto(loan);

    assertNotNull(dto);
    assertEquals(7L, dto.getId());
    assertEquals(3L, dto.getCustomerId());
    assertEquals(24, dto.getInstallmentCount());
    assertEquals(new BigDecimal("20000.00"), dto.getPrincipalLoanAmount());
    assertEquals(new BigDecimal("950.50"), dto.getMonthlyInstallmentAmount());
    assertEquals(new BigDecimal("2812.00"), dto.getInterestToBePaid());
    assertEquals(new BigDecimal("20000.00"), dto.getPrincipalToBePaid());
    assertEquals(new BigDecimal("15000.00"), dto.getRemainingPrincipal());
    assertEquals(LocalDate.of(2028, 9, 30), dto.getDueDate());
    assertEquals(LoaLoanStatusType.CONTINUING, dto.getLoanStatusType());
  }

  @Test
  void convertToLoaLoanDtoKeepsNullFieldsNull() {
    LoaLoanDto dto = mapper.convertToLoaLoanDto(new LoaLoan());

    assertEquals(new LoaLoanDto(), dto);
  }

  @ParameterizedTest
  @EnumSource(LoaLoanStatusType.class)
  void convertToLoaLoanDtoMapsEveryStatusType(LoaLoanStatusType statusType) {
    LoaLoanDto dto = mapper.convertToLoaLoanDto(buildLoan(statusType));

    assertEquals(statusType, dto.getLoanStatusType());
  }

  @Test
  void convertToLoaPayLoanOffResponseDtoReturnsNullForNullInput() {
    assertNull(mapper.convertToLoaPayLoanOffResponseDto(null));
  }

  @Test
  void convertToLoaPayLoanOffResponseDtoCopiesIdentityAndStatusOnly() {
    LoaLoan loan = buildLoan(LoaLoanStatusType.PAID);

    LoaPayLoanOffResponseDto dto = mapper.convertToLoaPayLoanOffResponseDto(loan);

    assertNotNull(dto);
    assertEquals(7L, dto.getId());
    assertEquals(3L, dto.getCustomerId());
    assertEquals(LoaLoanStatusType.PAID, dto.getLoanStatusType());
    assertNull(dto.getPaidAmount());
    assertNull(dto.getRemainingAmount());
  }

  @Test
  void convertToLoaPayLoanOffResponseDtoKeepsNullFieldsNull() {
    LoaPayLoanOffResponseDto dto = mapper.convertToLoaPayLoanOffResponseDto(new LoaLoan());

    assertEquals(new LoaPayLoanOffResponseDto(), dto);
  }

  @Test
  void mappingDoesNotMutateSource() {
    LoaLoan loan = buildLoan(LoaLoanStatusType.LATE);
    BaseAdditionalFields auditFields = loan.getBaseAdditionalFields();

    mapper.convertToLoaLoanDto(loan);
    mapper.convertToLoaPayLoanOffResponseDto(loan);

    assertEquals(new BigDecimal("15000.00"), loan.getRemainingPrincipal());
    assertEquals(LoaLoanStatusType.LATE, loan.getLoanStatusType());
    assertSame(auditFields, loan.getBaseAdditionalFields());
  }

  private static LoaLoan buildLoan(LoaLoanStatusType statusType) {
    LoaLoan loan = new LoaLoan();
    loan.setId(7L);
    loan.setCustomerId(3L);
    loan.setInstallmentCount(24);
    loan.setPrincipalLoanAmount(new BigDecimal("20000.00"));
    loan.setMonthlyInstallmentAmount(new BigDecimal("950.50"));
    loan.setInterestToBePaid(new BigDecimal("2812.00"));
    loan.setPrincipalToBePaid(new BigDecimal("20000.00"));
    loan.setRemainingPrincipal(new BigDecimal("15000.00"));
    loan.setDueDate(LocalDate.of(2028, 9, 30));
    loan.setLoanStatusType(statusType);
    loan.setBaseAdditionalFields(new BaseAdditionalFields());
    return loan;
  }
}
