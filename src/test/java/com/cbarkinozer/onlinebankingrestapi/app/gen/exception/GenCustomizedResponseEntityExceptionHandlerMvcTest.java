package com.cbarkinozer.onlinebankingrestapi.app.gen.exception;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cbarkinozer.onlinebankingrestapi.app.gen.enums.GenErrorMessage;
import com.cbarkinozer.onlinebankingrestapi.app.gen.exceptions.GenBusinessException;
import com.cbarkinozer.onlinebankingrestapi.app.gen.exceptions.IllegalFieldException;
import com.cbarkinozer.onlinebankingrestapi.app.gen.exceptions.ItemNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

class GenCustomizedResponseEntityExceptionHandlerMvcTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GenCustomizedResponseEntityExceptionHandler())
            .build();
  }

  @Test
  void unexpectedExceptionIsMappedToInternalServerError() throws Exception {
    mockMvc
        .perform(get("/test-errors/unexpected"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.messages").value("unexpected failure"))
        .andExpect(jsonPath("$.data.message").value("unexpected failure"))
        .andExpect(jsonPath("$.data.detail").value("uri=/test-errors/unexpected"));
  }

  @Test
  void itemNotFoundExceptionIsMappedToNotFound() throws Exception {
    mockMvc
        .perform(get("/test-errors/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.messages").value(GenErrorMessage.ITEM_NOT_FOUND.getMessage()))
        .andExpect(
            jsonPath("$.data.detail").value(GenErrorMessage.ITEM_NOT_FOUND.getDetailMessage()));
  }

  @Test
  void illegalFieldExceptionIsRoutedToItsOwnHandlerAndMappedToBadRequest() throws Exception {
    mockMvc
        .perform(get("/test-errors/illegal-field"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.messages").value(GenErrorMessage.VALUE_CANNOT_BE_NEGATIVE.getMessage()))
        .andExpect(
            jsonPath("$.data.detail")
                .value(GenErrorMessage.VALUE_CANNOT_BE_NEGATIVE.getDetailMessage()));
  }

  @Test
  void genBusinessExceptionIsRoutedToBusinessHandler() throws Exception {
    mockMvc
        .perform(get("/test-errors/business"))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.messages")
                .value(GenErrorMessage.DATE_COULD_NOT_BE_CONVERTED.getMessage()))
        .andExpect(
            jsonPath("$.data.detail")
                .value(GenErrorMessage.DATE_COULD_NOT_BE_CONVERTED.getDetailMessage()));
  }

  @Test
  void invalidRequestBodyIsMappedToBadRequestWithValidationMessage() throws Exception {
    mockMvc
        .perform(
            post("/test-errors/validated")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.messages").value("Validation failed!"))
        .andExpect(jsonPath("$.data.message").value("Validation failed!"))
        .andExpect(jsonPath("$.data.detail").value(containsString("name")));
  }

  @Test
  void validRequestBodyIsNotIntercepted() throws Exception {
    mockMvc
        .perform(
            post("/test-errors/validated")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"ok\"}"))
        .andExpect(status().isOk());
  }

  @ResponseBody
  @RequestMapping("/test-errors")
  static class ThrowingController {

    @GetMapping("/unexpected")
    String unexpected() {
      throw new IllegalStateException("unexpected failure");
    }

    @GetMapping("/not-found")
    String notFound() {
      throw new ItemNotFoundException(GenErrorMessage.ITEM_NOT_FOUND);
    }

    @GetMapping("/illegal-field")
    String illegalField() {
      throw new IllegalFieldException(GenErrorMessage.VALUE_CANNOT_BE_NEGATIVE);
    }

    @GetMapping("/business")
    String business() {
      throw new GenBusinessException(GenErrorMessage.DATE_COULD_NOT_BE_CONVERTED);
    }

    @PostMapping("/validated")
    String validated(@Valid @RequestBody NamedRequest request) {
      return request.getName();
    }
  }

  static class NamedRequest {

    @NotBlank private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
