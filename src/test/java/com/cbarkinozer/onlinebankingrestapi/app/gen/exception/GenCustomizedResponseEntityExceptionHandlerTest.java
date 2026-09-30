package com.cbarkinozer.onlinebankingrestapi.app.gen.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cbarkinozer.onlinebankingrestapi.app.gen.dto.RestResponse;
import com.cbarkinozer.onlinebankingrestapi.app.gen.enums.GenErrorMessage;
import com.cbarkinozer.onlinebankingrestapi.app.gen.exceptions.GenBusinessException;
import com.cbarkinozer.onlinebankingrestapi.app.gen.exceptions.IllegalFieldException;
import com.cbarkinozer.onlinebankingrestapi.app.gen.exceptions.ItemNotFoundException;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
class GenCustomizedResponseEntityExceptionHandlerTest {

  private static final String DESCRIPTION = "uri=/api/v1/test";

  @Mock private WebRequest webRequest;

  private GenCustomizedResponseEntityExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GenCustomizedResponseEntityExceptionHandler();
  }

  @Test
  void handleAllExceptionsReturnsInternalServerErrorWithExceptionMessageAndRequestDescription() {
    when(webRequest.getDescription(false)).thenReturn(DESCRIPTION);
    Date before = new Date();

    ResponseEntity<Object> response =
        handler.handleAllExceptions(new IllegalStateException("boom"), webRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    RestResponse<GenExceptionResponse> body = restResponseOf(response);
    assertErrorBody(body, "boom", DESCRIPTION);
    assertThat(body.getData().getErrorDate()).isAfterOrEqualTo(before).isBeforeOrEqualTo(new Date());
    verify(webRequest).getDescription(false);
  }

  @Test
  void handleAllExceptionsToleratesExceptionWithoutMessage() {
    when(webRequest.getDescription(false)).thenReturn(DESCRIPTION);

    ResponseEntity<Object> response =
        handler.handleAllExceptions(new RuntimeException(), webRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertErrorBody(restResponseOf(response), null, DESCRIPTION);
  }

  @Test
  void handleAllItemNotFoundExceptionReturnsNotFoundWithErrorMessageTexts() {
    ResponseEntity<Object> response =
        handler.handleAllItemNotFoundException(
            new ItemNotFoundException(GenErrorMessage.ITEM_NOT_FOUND), webRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertErrorBody(
        restResponseOf(response),
        GenErrorMessage.ITEM_NOT_FOUND.getMessage(),
        GenErrorMessage.ITEM_NOT_FOUND.getDetailMessage());
    verifyNoInteractions(webRequest);
  }

  @Test
  void handleAllGenBusinessExceptionReturnsErrorBodyWithErrorMessageTexts() {
    ResponseEntity<Object> response =
        handler.handleAllGenBusinessException(
            new GenBusinessException(GenErrorMessage.VALUE_CANNOT_BE_NEGATIVE), webRequest);

    assertThat(response.getStatusCode().isError()).isTrue();
    assertErrorBody(
        restResponseOf(response),
        GenErrorMessage.VALUE_CANNOT_BE_NEGATIVE.getMessage(),
        GenErrorMessage.VALUE_CANNOT_BE_NEGATIVE.getDetailMessage());
    verifyNoInteractions(webRequest);
  }

  @Test
  void handleAllIllegalFieldExceptionReturnsBadRequestWithErrorMessageTexts() {
    ResponseEntity<Object> response =
        handler.handleAllIllegalFieldException(
            new IllegalFieldException(GenErrorMessage.PARAMETER_CANNOT_BE_NULL), webRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertErrorBody(
        restResponseOf(response),
        GenErrorMessage.PARAMETER_CANNOT_BE_NULL.getMessage(),
        GenErrorMessage.PARAMETER_CANNOT_BE_NULL.getDetailMessage());
    verifyNoInteractions(webRequest);
  }

  @Test
  void handleMethodArgumentNotValidReturnsBadRequestWithBindingResultDescription()
      throws NoSuchMethodException {
    MethodArgumentNotValidException ex = methodArgumentNotValidException();

    ResponseEntity<Object> response =
        handler.handleMethodArgumentNotValid(
            ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    RestResponse<GenExceptionResponse> body = restResponseOf(response);
    assertErrorBody(body, "Validation failed!", ex.getBindingResult().toString());
    assertThat(body.getData().getDetail()).contains("name").contains("must not be blank");
  }

  @Test
  void inheritedHandleExceptionDelegatesMethodArgumentNotValidToOverride() throws Exception {
    MethodArgumentNotValidException ex = methodArgumentNotValidException();

    ResponseEntity<Object> response = handler.handleException(ex, webRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertErrorBody(restResponseOf(response), "Validation failed!", ex.getBindingResult().toString());
  }

  private static MethodArgumentNotValidException methodArgumentNotValidException()
      throws NoSuchMethodException {
    BindingResult bindingResult = new BeanPropertyBindingResult(new Payload(), "payload");
    bindingResult.addError(new FieldError("payload", "name", "must not be blank"));
    MethodParameter parameter =
        new MethodParameter(Payload.class.getDeclaredMethod("setName", String.class), 0);
    return new MethodArgumentNotValidException(parameter, bindingResult);
  }

  private static void assertErrorBody(
      RestResponse<GenExceptionResponse> body, String message, String detail) {
    assertThat(body).isNotNull();
    assertThat(body.isSuccess()).isFalse();
    assertThat(body.getResponseDate()).isNotNull();
    assertThat(body.getMessages()).isEqualTo(message);
    assertThat(body.getData()).isNotNull();
    assertThat(body.getData().getMessage()).isEqualTo(message);
    assertThat(body.getData().getDetail()).isEqualTo(detail);
    assertThat(body.getData().getErrorDate()).isNotNull();
  }

  @SuppressWarnings("unchecked")
  private static RestResponse<GenExceptionResponse> restResponseOf(ResponseEntity<Object> response) {
    assertThat(response.getBody()).isInstanceOf(RestResponse.class);
    return (RestResponse<GenExceptionResponse>) response.getBody();
  }

  static class Payload {

    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
