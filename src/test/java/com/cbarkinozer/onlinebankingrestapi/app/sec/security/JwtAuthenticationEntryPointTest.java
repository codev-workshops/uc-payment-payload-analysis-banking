package com.cbarkinozer.onlinebankingrestapi.app.sec.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

class JwtAuthenticationEntryPointTest {

  private final JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint();

  @Test
  void shouldSendUnauthorizedErrorWithExceptionMessage() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    entryPoint.commence(request, response, new BadCredentialsException("Bad credentials"));

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    assertEquals("Bad credentials", response.getErrorMessage());
  }
}
