package com.cbarkinozer.onlinebankingrestapi.app.sec.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenGeneratorTest {

  private static final String APP_KEY = "ONLINEBANKINGRESTAPI";
  private static final String OTHER_KEY = "OTHERSECRETKEY";
  private static final long EXPIRE_TIME = 86_400_000L;

  private JwtTokenGenerator jwtTokenGenerator;

  @BeforeEach
  void setUp() {
    jwtTokenGenerator = createGenerator(APP_KEY, EXPIRE_TIME);
  }

  @Test
  void shouldGenerateTokenWithUserIdSubjectAndExpiration() {
    long before = System.currentTimeMillis();

    String token = jwtTokenGenerator.generateJwtToken(createAuthentication(42L));

    Claims claims = Jwts.parser().setSigningKey(APP_KEY).parseClaimsJws(token).getBody();
    assertEquals("42", claims.getSubject());
    assertNotNull(claims.getIssuedAt());
    long expectedExpiry = before + EXPIRE_TIME;
    long actualExpiry = claims.getExpiration().getTime();
    assertTrue(Math.abs(actualExpiry - expectedExpiry) < 5_000L);
  }

  @Test
  void shouldFindUserIdByToken() {
    String token = jwtTokenGenerator.generateJwtToken(createAuthentication(42L));

    assertEquals(42L, jwtTokenGenerator.findUserIdByToken(token));
  }

  @Test
  void shouldThrowWhenFindingUserIdOfTokenSignedWithOtherKey() {
    String token = createGenerator(OTHER_KEY, EXPIRE_TIME)
        .generateJwtToken(createAuthentication(42L));

    assertThrows(SignatureException.class, () -> jwtTokenGenerator.findUserIdByToken(token));
  }

  @Test
  void shouldThrowWhenFindingUserIdOfMalformedToken() {
    assertThrows(MalformedJwtException.class,
        () -> jwtTokenGenerator.findUserIdByToken("not-a-jwt"));
  }

  @Test
  void shouldThrowWhenFindingUserIdOfExpiredToken() {
    String token = createGenerator(APP_KEY, -60_000L).generateJwtToken(createAuthentication(1L));

    assertThrows(ExpiredJwtException.class, () -> jwtTokenGenerator.findUserIdByToken(token));
  }

  @Test
  void shouldValidateFreshToken() {
    String token = jwtTokenGenerator.generateJwtToken(createAuthentication(1L));

    assertTrue(jwtTokenGenerator.validateToken(token));
  }

  @Test
  void shouldRejectExpiredToken() {
    String token = createGenerator(APP_KEY, -60_000L).generateJwtToken(createAuthentication(1L));

    assertFalse(jwtTokenGenerator.validateToken(token));
  }

  @Test
  void shouldRejectTokenSignedWithOtherKey() {
    String token = createGenerator(OTHER_KEY, EXPIRE_TIME)
        .generateJwtToken(createAuthentication(1L));

    assertFalse(jwtTokenGenerator.validateToken(token));
  }

  @Test
  void shouldRejectMalformedToken() {
    assertFalse(jwtTokenGenerator.validateToken("not-a-jwt"));
  }

  @Test
  void shouldRejectTamperedToken() {
    String token = jwtTokenGenerator.generateJwtToken(createAuthentication(1L));
    String tampered = token.substring(0, token.length() - 2) + "xx";

    assertFalse(jwtTokenGenerator.validateToken(tampered));
  }

  @Test
  void shouldRejectTokenWithoutExpiration() {
    String token = Jwts.builder()
        .setSubject("1")
        .setIssuedAt(new Date())
        .signWith(SignatureAlgorithm.HS512, APP_KEY)
        .compact();

    assertFalse(jwtTokenGenerator.validateToken(token));
  }

  @Test
  void shouldRejectUnsignedToken() {
    String token = Jwts.builder()
        .setSubject("1")
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
        .compact();

    assertFalse(jwtTokenGenerator.validateToken(token));
  }

  private static JwtTokenGenerator createGenerator(String appKey, long expireTime) {
    JwtTokenGenerator generator = new JwtTokenGenerator();
    ReflectionTestUtils.setField(generator, "APP_KEY", appKey);
    ReflectionTestUtils.setField(generator, "EXPIRE_TIME", expireTime);
    return generator;
  }

  private static Authentication createAuthentication(Long userId) {
    JwtUserDetails principal =
        JwtUserDetails.create(JwtUserDetailsTest.createCustomer(userId, 12345L, "pw"));
    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }
}
