package com.cbarkinozer.onlinebankingrestapi.app.sec.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cbarkinozer.onlinebankingrestapi.app.sec.security.JwtAuthenticationEntryPoint;
import com.cbarkinozer.onlinebankingrestapi.app.sec.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.CorsFilter;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

  private static final String ORIGIN = "https://client.example.com";

  @Mock private UserDetailsService userDetailsService;

  @Mock private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  private SecurityConfig securityConfig;

  @BeforeEach
  void setUp() {
    securityConfig = new SecurityConfig(userDetailsService, jwtAuthenticationEntryPoint);
  }

  @Test
  void shouldCreateNewJwtAuthenticationFilterOnEachCall() {
    JwtAuthenticationFilter first = securityConfig.jwtAuthenticationFilter();
    JwtAuthenticationFilter second = securityConfig.jwtAuthenticationFilter();

    assertNotNull(first);
    assertNotSame(first, second);
  }

  @Test
  void shouldProvideBcryptPasswordEncoder() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

    assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
  }

  @Test
  void passwordEncoderShouldHashAndMatchRawPassword() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

    String encoded = passwordEncoder.encode("secret");

    assertNotEquals("secret", encoded);
    assertTrue(encoded.startsWith("$2"));
    assertTrue(passwordEncoder.matches("secret", encoded));
    assertFalse(passwordEncoder.matches("wrong", encoded));
  }

  @Test
  void passwordEncoderShouldProduceSaltedHashes() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

    assertNotEquals(passwordEncoder.encode("secret"), passwordEncoder.encode("secret"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD"})
  void corsFilterShouldAllowPreflightForDeclaredMethods(String method) throws Exception {
    MockHttpServletResponse response = preflight("/any/path", method);

    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertEquals(ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    assertEquals("true", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    assertTrue(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).contains(method));
  }

  @Test
  void corsFilterShouldRejectPreflightForUndeclaredMethod() throws Exception {
    MockHttpServletResponse response = preflight("/any/path", "TRACE");

    assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
  }

  @Test
  void corsFilterShouldAllowAnyRequestHeader() throws Exception {
    MockHttpServletRequest request = preflightRequest("/any/path", "POST");
    request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, X-Custom");
    MockHttpServletResponse response = new MockHttpServletResponse();

    securityConfig.corsFilter().doFilter(request, response, new MockFilterChain());

    assertEquals(HttpStatus.OK.value(), response.getStatus());
    String allowedHeaders = response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
    assertTrue(allowedHeaders.contains("Authorization"));
    assertTrue(allowedHeaders.contains("X-Custom"));
  }

  @Test
  void corsFilterShouldReflectAnyOriginOnActualRequestAndContinueChain() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/accounts");
    request.addHeader(HttpHeaders.ORIGIN, "http://another-origin.test:3000");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    securityConfig.corsFilter().doFilter(request, response, chain);

    assertEquals(
        "http://another-origin.test:3000",
        response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    assertEquals("true", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    assertNotNull(chain.getRequest());
  }

  @Test
  void corsFilterShouldPassThroughNonCorsRequest() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/accounts");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    CorsFilter corsFilter = securityConfig.corsFilter();
    corsFilter.doFilter(request, response, chain);

    assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    assertNotNull(chain.getRequest());
  }

  private MockHttpServletResponse preflight(String uri, String requestedMethod)
      throws Exception {
    MockHttpServletRequest request = preflightRequest(uri, requestedMethod);
    MockHttpServletResponse response = new MockHttpServletResponse();

    securityConfig.corsFilter().doFilter(request, response, new MockFilterChain());

    return response;
  }

  private static MockHttpServletRequest preflightRequest(String uri, String requestedMethod) {
    MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", uri);
    request.addHeader(HttpHeaders.ORIGIN, ORIGIN);
    request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, requestedMethod);
    return request;
  }
}
