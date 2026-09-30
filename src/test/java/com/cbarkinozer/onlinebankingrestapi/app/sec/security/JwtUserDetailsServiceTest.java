package com.cbarkinozer.onlinebankingrestapi.app.sec.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.cbarkinozer.onlinebankingrestapi.app.cus.entity.CusCustomer;
import com.cbarkinozer.onlinebankingrestapi.app.cus.enums.CusErrorMessage;
import com.cbarkinozer.onlinebankingrestapi.app.cus.service.entityservice.CusCustomerEntityService;
import com.cbarkinozer.onlinebankingrestapi.app.gen.enums.GenErrorMessage;
import com.cbarkinozer.onlinebankingrestapi.app.gen.exceptions.ItemNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JwtUserDetailsServiceTest {

  @Mock
  private CusCustomerEntityService cusCustomerEntityService;

  @InjectMocks
  private JwtUserDetailsService jwtUserDetailsService;

  @Test
  void shouldLoadUserByUsernameUsingIdentityNo() {
    CusCustomer customer = JwtUserDetailsTest.createCustomer(3L, 12345678901L, "pw");
    when(cusCustomerEntityService.findByIdentityNo(12345678901L)).thenReturn(customer);

    UserDetails result = jwtUserDetailsService.loadUserByUsername("12345678901");

    JwtUserDetails jwtUserDetails = assertInstanceOf(JwtUserDetails.class, result);
    assertEquals(3L, jwtUserDetails.getId());
    assertEquals("12345678901", jwtUserDetails.getUsername());
    assertEquals("pw", jwtUserDetails.getPassword());
  }

  @Test
  void shouldPropagateNotFoundWhenLoadingUnknownUsername() {
    when(cusCustomerEntityService.findByIdentityNo(1L))
        .thenThrow(new ItemNotFoundException(CusErrorMessage.CUSTOMER_NOT_FOUND));

    assertThrows(ItemNotFoundException.class,
        () -> jwtUserDetailsService.loadUserByUsername("1"));
  }

  @Test
  void shouldLoadUserByUserId() {
    CusCustomer customer = JwtUserDetailsTest.createCustomer(9L, 111L, "pw");
    when(cusCustomerEntityService.getByIdWithControl(9L)).thenReturn(customer);

    UserDetails result = jwtUserDetailsService.loadUserByUserId(9L);

    JwtUserDetails jwtUserDetails = assertInstanceOf(JwtUserDetails.class, result);
    assertEquals(9L, jwtUserDetails.getId());
    assertEquals("111", jwtUserDetails.getUsername());
  }

  @Test
  void shouldPropagateNotFoundWhenLoadingUnknownUserId() {
    when(cusCustomerEntityService.getByIdWithControl(99L))
        .thenThrow(new ItemNotFoundException(GenErrorMessage.ITEM_NOT_FOUND));

    assertThrows(ItemNotFoundException.class,
        () -> jwtUserDetailsService.loadUserByUserId(99L));
  }
}
