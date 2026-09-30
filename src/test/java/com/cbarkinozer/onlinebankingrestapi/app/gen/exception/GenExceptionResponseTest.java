package com.cbarkinozer.onlinebankingrestapi.app.gen.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import org.junit.jupiter.api.Test;

class GenExceptionResponseTest {

  private static final Date ERROR_DATE = new Date(1_000L);

  @Test
  void allArgsConstructorPopulatesFields() {
    GenExceptionResponse response = new GenExceptionResponse(ERROR_DATE, "message", "detail");

    assertThat(response.getErrorDate()).isEqualTo(ERROR_DATE);
    assertThat(response.getMessage()).isEqualTo("message");
    assertThat(response.getDetail()).isEqualTo("detail");
  }

  @Test
  void settersOverrideFields() {
    GenExceptionResponse response = new GenExceptionResponse(null, null, null);
    Date otherDate = new Date(2_000L);

    response.setErrorDate(otherDate);
    response.setMessage("other message");
    response.setDetail("other detail");

    assertThat(response.getErrorDate()).isEqualTo(otherDate);
    assertThat(response.getMessage()).isEqualTo("other message");
    assertThat(response.getDetail()).isEqualTo("other detail");
  }

  @Test
  void equalsAndHashCodeAreValueBased() {
    GenExceptionResponse first = new GenExceptionResponse(ERROR_DATE, "message", "detail");
    GenExceptionResponse second = new GenExceptionResponse(ERROR_DATE, "message", "detail");

    assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    assertThat(first).isEqualTo(first);
  }

  @Test
  void equalsDetectsDifferentFields() {
    GenExceptionResponse base = new GenExceptionResponse(ERROR_DATE, "message", "detail");

    assertThat(base).isNotEqualTo(new GenExceptionResponse(new Date(3_000L), "message", "detail"));
    assertThat(base).isNotEqualTo(new GenExceptionResponse(ERROR_DATE, "other", "detail"));
    assertThat(base).isNotEqualTo(new GenExceptionResponse(ERROR_DATE, "message", "other"));
    assertThat(base).isNotEqualTo(new GenExceptionResponse(null, null, null));
    assertThat(new GenExceptionResponse(null, null, null))
        .isEqualTo(new GenExceptionResponse(null, null, null));
    assertThat(base).isNotEqualTo(null);
    assertThat(base).isNotEqualTo("message");
  }

  @Test
  void toStringContainsAllFields() {
    GenExceptionResponse response = new GenExceptionResponse(ERROR_DATE, "message", "detail");

    assertThat(response.toString())
        .contains("errorDate=" + ERROR_DATE)
        .contains("message=message")
        .contains("detail=detail");
  }
}
