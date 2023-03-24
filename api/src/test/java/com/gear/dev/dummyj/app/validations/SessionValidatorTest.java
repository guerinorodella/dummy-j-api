package com.gear.dev.dummyj.app.validations;

import com.gear.dev.dummyj.core.services.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SessionValidatorTest {

  private AuthorizationService authService;
  private SessionValidator instance;

  @BeforeEach
  void setUp() {
    authService = Mockito.mock(AuthorizationService.class);
    instance = new SessionValidator(authService);
  }

  @Test
  void missingAuthorizationHeader_isInvalid() {
    var response = instance.validateSession(new HttpHeaders());

    assertNotNull(response);
    assertEquals(-800, response.getErrorCode());
    assertEquals("Missing Authorization header", response.getMessage());
    assertNull(response.getData());
    verifyNoInteractions(authService);
  }

  @Test
  void authorizationHeader_containsEmptyValue_isInvalid() {
    var emptyAuthorization = new HttpHeaders();
    emptyAuthorization.set(HttpHeaders.AUTHORIZATION, "");

    var response = instance.validateSession(emptyAuthorization);

    assertNotNull(response);
    assertEquals(-801, response.getErrorCode());
    assertEquals("Invalid bearer received", response.getMessage());
    assertNull(response.getData());
    verifyNoInteractions(authService);
  }

  @Test
  void invalidAuthorizationHeader_isInvalid() {
    var emptyAuthorization = new HttpHeaders();
    emptyAuthorization.set(HttpHeaders.AUTHORIZATION, "BEA");

    var response = instance.validateSession(emptyAuthorization);

    assertNotNull(response);
    assertEquals(-801, response.getErrorCode());
    assertEquals("Invalid bearer received", response.getMessage());
    assertNull(response.getData());
    verifyNoInteractions(authService);
  }

  @Test
  void invalidTokenOnAuthorizationHeader_isInvalid() {
    String token = "123456789";
    var invalidSessionTokenHeader = new HttpHeaders();
    invalidSessionTokenHeader.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    when(authService.isUserAuthenticated(eq(token))).thenReturn(false);
    var tokenCaptor = ArgumentCaptor.forClass(String.class);

    var response = instance.validateSession(invalidSessionTokenHeader);

    assertNotNull(response);
    assertEquals(-802, response.getErrorCode());
    assertEquals("Unauthorized or expired token", response.getMessage());
    assertNull(response.getData());
    verify(authService, atLeastOnce()).isUserAuthenticated(tokenCaptor.capture());
    assertEquals(token, tokenCaptor.getValue());
  }

  @Test
  void validTokenOnAuthorizationHeader_isValid() {
    String token = "123456789ABCDE";
    var invalidSessionTokenHeader = new HttpHeaders();
    invalidSessionTokenHeader.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    when(authService.isUserAuthenticated(eq(token))).thenReturn(true);
    var tokenCaptor = ArgumentCaptor.forClass(String.class);

    var response = instance.validateSession(invalidSessionTokenHeader);

    assertNotNull(response);
    assertEquals(0, response.getErrorCode());
    assertEquals("Success", response.getMessage());
    assertNotNull(response.getData());
    assertEquals(token, response.getData());
    verify(authService, atLeastOnce()).isUserAuthenticated(tokenCaptor.capture());
    assertEquals(token, tokenCaptor.getValue());
  }
}