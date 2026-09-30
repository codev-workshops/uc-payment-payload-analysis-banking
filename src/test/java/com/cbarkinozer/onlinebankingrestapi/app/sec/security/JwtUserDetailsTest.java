package com.cbarkinozer.onlinebankingrestapi.app.sec.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cbarkinozer.onlinebankingrestapi.app.cus.entity.CusCustomer;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class JwtUserDetailsTest {

  @Test
  void shouldCreateUserDetailsFromCustomer() {
    CusCustomer customer = createCustomer(7L, 12345678901L, "secret");

    JwtUserDetails userDetails = JwtUserDetails.create(customer);

    assertEquals(7L, userDetails.getId());
    assertEquals("12345678901", userDetails.getUsername());
    assertEquals("secret", userDetails.getPassword());
  }

  @Test
  void shouldGrantSingleUserAuthority() {
    JwtUserDetails userDetails = JwtUserDetails.create(createCustomer(1L, 1L, "pw"));

    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

    assertEquals(1, authorities.size());
    assertEquals("user", authorities.iterator().next().getAuthority());
  }

  @Test
  void shouldReportAccountAsActive() {
    JwtUserDetails userDetails = JwtUserDetails.create(createCustomer(1L, 1L, "pw"));

    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isEnabled());
  }

  static CusCustomer createCustomer(Long id, Long identityNo, String password) {
    CusCustomer customer = new CusCustomer();
    customer.setId(id);
    customer.setIdentityNo(identityNo);
    customer.setPassword(password);
    customer.setName("John");
    customer.setSurname("Doe");
    return customer;
  }
}
