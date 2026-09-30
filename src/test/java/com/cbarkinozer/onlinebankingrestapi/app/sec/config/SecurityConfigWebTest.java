package com.cbarkinozer.onlinebankingrestapi.app.sec.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cbarkinozer.onlinebankingrestapi.app.sec.security.JwtAuthenticationEntryPoint;
import com.cbarkinozer.onlinebankingrestapi.app.sec.security.JwtAuthenticationFilter;
import com.cbarkinozer.onlinebankingrestapi.app.sec.security.JwtTokenGenerator;
import com.cbarkinozer.onlinebankingrestapi.app.sec.security.JwtUserDetailsService;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import javax.servlet.Filter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringJUnitWebConfig(
    classes = {
      SecurityConfig.class,
      JwtAuthenticationEntryPoint.class,
      SecurityConfigWebTest.TestWebConfig.class
    })
class SecurityConfigWebTest {

  private static final String VALID_TOKEN = "valid-token";
  private static final String BEARER_VALID_TOKEN = "Bearer " + VALID_TOKEN;
  private static final Long USER_ID = 42L;
  private static final String IDENTITY_NO = "12345678901";

  @MockBean private JwtUserDetailsService jwtUserDetailsService;

  @MockBean private JwtTokenGenerator jwtTokenGenerator;

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private FilterChainProxy springSecurityFilterChain;

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private PasswordEncoder passwordEncoder;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilters(springSecurityFilterChain)
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldPermitLoginWithoutTokenAndWithoutCsrfToken() throws Exception {
    mockMvc.perform(post("/auth/login")).andExpect(status().isOk()).andExpect(content().string("login"));

    verify(jwtTokenGenerator, never()).validateToken(anyString());
  }

  @Test
  void shouldPermitRegisterWithoutToken() throws Exception {
    mockMvc.perform(post("/auth/register")).andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/swagger-ui/index.html",
        "/swagger-ui/nested/resource.js",
        "/swagger-ui.html",
        "/v3/api-docs",
        "/v3/api-docs/swagger-config"
      })
  void shouldPermitDocumentationEndpointsWithoutToken(String path) throws Exception {
    mockMvc.perform(get(path)).andExpect(status().isOk()).andExpect(content().string("docs"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/accounts", "/customers/1", "/authentication", "/v3/other", "/"})
  void shouldReturnUnauthorizedForProtectedEndpointsWithoutToken(String path) throws Exception {
    mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
    when(jwtTokenGenerator.validateToken(VALID_TOKEN)).thenReturn(false);

    mockMvc
        .perform(get("/accounts").header(HttpHeaders.AUTHORIZATION, BEARER_VALID_TOKEN))
        .andExpect(status().isUnauthorized());

    verify(jwtTokenGenerator, never()).findUserIdByToken(anyString());
  }

  @Test
  void shouldIgnoreAuthorizationHeaderWithoutBearerPrefix() throws Exception {
    mockMvc
        .perform(get("/accounts").header(HttpHeaders.AUTHORIZATION, VALID_TOKEN))
        .andExpect(status().isUnauthorized());

    verify(jwtTokenGenerator, never()).validateToken(anyString());
  }

  @Test
  void shouldReturnUnauthorizedWhenUserDetailsCannotBeResolved() throws Exception {
    when(jwtTokenGenerator.validateToken(VALID_TOKEN)).thenReturn(true);
    when(jwtTokenGenerator.findUserIdByToken(VALID_TOKEN)).thenReturn(USER_ID);
    when(jwtUserDetailsService.loadUserByUserId(USER_ID)).thenReturn(null);

    mockMvc
        .perform(get("/accounts").header(HttpHeaders.AUTHORIZATION, BEARER_VALID_TOKEN))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldAllowProtectedEndpointWithValidToken() throws Exception {
    stubValidToken();

    mockMvc
        .perform(get("/accounts").header(HttpHeaders.AUTHORIZATION, BEARER_VALID_TOKEN))
        .andExpect(status().isOk())
        .andExpect(content().string("accounts:" + IDENTITY_NO));
  }

  @Test
  void shouldAllowProtectedPostWithValidTokenWithoutCsrfToken() throws Exception {
    stubValidToken();

    mockMvc
        .perform(post("/accounts").header(HttpHeaders.AUTHORIZATION, BEARER_VALID_TOKEN))
        .andExpect(status().isOk())
        .andExpect(content().string("created"));
  }

  @Test
  void shouldNotCreateHttpSession() throws Exception {
    stubValidToken();

    MvcResult result =
        mockMvc
            .perform(get("/accounts").header(HttpHeaders.AUTHORIZATION, BEARER_VALID_TOKEN))
            .andExpect(status().isOk())
            .andReturn();

    assertNull(result.getRequest().getSession(false));
    assertNull(result.getResponse().getCookie("JSESSIONID"));
  }

  @Test
  void shouldNotReuseAuthenticationBetweenStatelessRequests() throws Exception {
    stubValidToken();
    mockMvc
        .perform(get("/accounts").header(HttpHeaders.AUTHORIZATION, BEARER_VALID_TOKEN))
        .andExpect(status().isOk());

    mockMvc.perform(get("/accounts")).andExpect(status().isUnauthorized());
  }

  @Test
  void shouldAnswerCorsPreflightForProtectedEndpointWithoutToken() throws Exception {
    mockMvc
        .perform(
            options("/accounts")
                .header(HttpHeaders.ORIGIN, "https://client.example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
        .andExpect(status().isOk())
        .andExpect(
            header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://client.example.com"))
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
  }

  @Test
  void shouldRegisterCorsAndJwtFiltersInSecurityChainInExpectedOrder() {
    List<SecurityFilterChain> chains = springSecurityFilterChain.getFilterChains();
    assertEquals(1, chains.size());
    List<Filter> filters = chains.get(0).getFilters();

    int corsIndex = indexOf(filters, CorsFilter.class);
    int jwtIndex = indexOf(filters, JwtAuthenticationFilter.class);
    int usernamePasswordIndex = indexOf(filters, UsernamePasswordAuthenticationFilter.class);

    assertTrue(corsIndex >= 0);
    assertTrue(jwtIndex >= 0);
    assertTrue(corsIndex < jwtIndex);
    assertTrue(usernamePasswordIndex < 0 || jwtIndex < usernamePasswordIndex);
    assertEquals(1, filters.stream().filter(JwtAuthenticationFilter.class::isInstance).count());
  }

  @Test
  void authenticationManagerShouldAuthenticateWithUserDetailsServiceAndBcrypt() {
    stubUserByIdentityNo("secret");

    Authentication result =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(IDENTITY_NO, "secret"));

    assertTrue(result.isAuthenticated());
    assertEquals(IDENTITY_NO, result.getName());
  }

  @Test
  void authenticationManagerShouldRejectWrongPassword() {
    stubUserByIdentityNo("secret");

    UsernamePasswordAuthenticationToken attempt =
        new UsernamePasswordAuthenticationToken(IDENTITY_NO, "wrong");

    assertThrows(BadCredentialsException.class, () -> authenticationManager.authenticate(attempt));
  }

  @Test
  void authenticationManagerShouldRejectUnknownUser() {
    when(jwtUserDetailsService.loadUserByUsername(IDENTITY_NO))
        .thenThrow(new UsernameNotFoundException("not found"));

    UsernamePasswordAuthenticationToken attempt =
        new UsernamePasswordAuthenticationToken(IDENTITY_NO, "secret");

    assertThrows(BadCredentialsException.class, () -> authenticationManager.authenticate(attempt));
  }

  @Test
  void passwordEncoderBeanShouldBeSingletonBcrypt() {
    String encoded = passwordEncoder.encode("secret");

    assertTrue(passwordEncoder.matches("secret", encoded));
    assertFalse(passwordEncoder.matches("secret2", encoded));
    assertEquals(passwordEncoder, webApplicationContext.getBean(PasswordEncoder.class));
  }

  private void stubValidToken() {
    when(jwtTokenGenerator.validateToken(VALID_TOKEN)).thenReturn(true);
    when(jwtTokenGenerator.findUserIdByToken(VALID_TOKEN)).thenReturn(USER_ID);
    when(jwtUserDetailsService.loadUserByUserId(USER_ID)).thenReturn(user("ignored"));
  }

  private void stubUserByIdentityNo(String rawPassword) {
    when(jwtUserDetailsService.loadUserByUsername(IDENTITY_NO))
        .thenReturn(user(passwordEncoder.encode(rawPassword)));
  }

  private static UserDetails user(String encodedPassword) {
    return new User(IDENTITY_NO, encodedPassword, Collections.emptyList());
  }

  private static int indexOf(List<Filter> filters, Class<? extends Filter> type) {
    for (int i = 0; i < filters.size(); i++) {
      if (type.isInstance(filters.get(i))) {
        return i;
      }
    }
    return -1;
  }

  @EnableWebMvc
  static class TestWebConfig {

    @Bean
    TestEndpoints testEndpoints() {
      return new TestEndpoints();
    }
  }

  @RequestMapping
  @ResponseBody
  static class TestEndpoints {

    @PostMapping({"/auth/login", "/auth/register"})
    String login() {
      return "login";
    }

    @GetMapping({"/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/**"})
    String docs() {
      return "docs";
    }

    @GetMapping("/accounts")
    String accounts(Principal principal) {
      return "accounts:" + principal.getName();
    }

    @PostMapping("/accounts")
    String createAccount() {
      return "created";
    }
  }
}
