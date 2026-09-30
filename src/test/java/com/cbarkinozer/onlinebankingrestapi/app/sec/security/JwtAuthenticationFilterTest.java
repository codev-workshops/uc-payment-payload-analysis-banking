package com.cbarkinozer.onlinebankingrestapi.app.sec.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  private static final String TOKEN = "header.payload.signature";

  @Mock
  private JwtTokenGenerator jwtTokenGenerator;

  @Mock
  private JwtUserDetailsService jwtUserDetailsService;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private final MockHttpServletRequest request = new MockHttpServletRequest();
  private final MockHttpServletResponse response = new MockHttpServletResponse();

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldContinueChainWithoutAuthenticationWhenHeaderMissing() throws Exception {
    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtTokenGenerator, jwtUserDetailsService);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldIgnoreHeaderWithoutBearerPrefix() throws Exception {
    request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtTokenGenerator, jwtUserDetailsService);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldIgnoreBearerPrefixWithoutToken() throws Exception {
    request.addHeader("Authorization", "Bearer ");

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtTokenGenerator, jwtUserDetailsService);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldNotAuthenticateWhenTokenInvalid() throws Exception {
    request.addHeader("Authorization", "Bearer " + TOKEN);
    when(jwtTokenGenerator.validateToken(TOKEN)).thenReturn(false);

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(jwtTokenGenerator, never()).findUserIdByToken(TOKEN);
    verifyNoInteractions(jwtUserDetailsService);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldAuthenticateWhenTokenValid() throws Exception {
    request.addHeader("Authorization", "Bearer " + TOKEN);
    request.setRemoteAddr("10.0.0.1");
    JwtUserDetails userDetails =
        JwtUserDetails.create(JwtUserDetailsTest.createCustomer(5L, 12345L, "pw"));
    when(jwtTokenGenerator.validateToken(TOKEN)).thenReturn(true);
    when(jwtTokenGenerator.findUserIdByToken(TOKEN)).thenReturn(5L);
    when(jwtUserDetailsService.loadUserByUserId(5L)).thenReturn(userDetails);

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertSame(userDetails, authentication.getPrincipal());
    assertNull(authentication.getCredentials());
    assertEquals(userDetails.getAuthorities(), authentication.getAuthorities());
    WebAuthenticationDetails details =
        assertInstanceOf(WebAuthenticationDetails.class, authentication.getDetails());
    assertEquals("10.0.0.1", details.getRemoteAddress());
  }

  @Test
  void shouldNotAuthenticateWhenUserDetailsNotFound() throws Exception {
    request.addHeader("Authorization", "Bearer " + TOKEN);
    when(jwtTokenGenerator.validateToken(TOKEN)).thenReturn(true);
    when(jwtTokenGenerator.findUserIdByToken(TOKEN)).thenReturn(5L);
    when(jwtUserDetailsService.loadUserByUserId(5L)).thenReturn(null);

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }
}
