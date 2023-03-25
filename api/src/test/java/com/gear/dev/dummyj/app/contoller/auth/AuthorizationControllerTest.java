package com.gear.dev.dummyj.app.contoller.auth;

import com.gear.dev.dummyj.app.contoller.BaseResponse;
import com.gear.dev.dummyj.app.validations.SessionValidator;
import com.gear.dev.dummyj.core.model.TokenModel;
import com.gear.dev.dummyj.core.model.UserModel;
import com.gear.dev.dummyj.core.repository.TokenRepository;
import com.gear.dev.dummyj.core.repository.UserRepository;
import com.gear.dev.dummyj.core.services.AuthorizationService;
import com.gear.dev.dummyj.core.services.AuthorizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.gear.dev.dummyj.core.services.AuthorizationServiceImpl.BLOCKED;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@DataJpaTest
@ActiveProfiles("test")
class AuthorizationControllerTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private TokenRepository tokenRepository;
  private AuthorizationService authService;
  private AuthorizationController instance;
  private SessionValidator sessionValidator;
  private UserModel defaultUserModel;


  @BeforeEach
  void setup() {
    defaultUserModel = new UserModel(42L, "foo", "123456", "foo@mail.com", now(), 0);
    authService = new AuthorizationServiceImpl(userRepository, tokenRepository);
    sessionValidator = new SessionValidator(authService);
    instance = new AuthorizationController(authService, sessionValidator);
  }

  @Test
  void authenticateUser_returnsFailureResponse_whenUserDoesNotExistsOnDB() {
    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.when(userRepository.findByUserNameAndPassword(stringCaptor.capture(), stringCaptor.capture())).thenReturn(null);
    AuthRequest authRequest = new AuthRequest(defaultUserModel.getUserName(), defaultUserModel.getPassword());
    ResponseEntity expectedResponse = ResponseEntity.status(NOT_FOUND)
            .body(new BaseResponse<AuthResponse>(
                    -1,
                    "User not found",
                    new AuthResponse(null, "NOT_FOUND")));

    var response = instance.authenticateUser(authRequest);

    assertNotNull(response);
    assertTrue(new ReflectionEquals(expectedResponse).matches(response));
    List<String> allValuesCaptured = stringCaptor.getAllValues();
    assertEquals(authRequest.getUserName(), allValuesCaptured.get(0));
    assertEquals(authRequest.getPassword(), allValuesCaptured.get(1));
  }

  @Test
  void authenticateUser_returnsBlockedResponse_whenUserIsBlocked() {
    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    defaultUserModel.setStatus(BLOCKED);
    Mockito.when(userRepository.findByUserNameAndPassword(stringCaptor.capture(), stringCaptor.capture())).thenReturn(defaultUserModel);

    AuthRequest authRequest = new AuthRequest(defaultUserModel.getUserName(), defaultUserModel.getPassword());
    ResponseEntity userIsBlockedResponse = ResponseEntity.status(OK)
            .body(new BaseResponse<AuthResponse>(
                    -2,
                    "User is blocked",
                    new AuthResponse(null, "BLOCKED")));

    var response = instance.authenticateUser(authRequest);

    assertNotNull(response);
    assertTrue(new ReflectionEquals(userIsBlockedResponse).matches(response));
    List<String> allValuesCaptured = stringCaptor.getAllValues();
    assertEquals(authRequest.getUserName(), allValuesCaptured.get(0));
    assertEquals(authRequest.getPassword(), allValuesCaptured.get(1));
  }

  @Test
  void authenticateUser_returnsSuccessResponse_whenUserIsValid() {
    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.when(userRepository.findByUserNameAndPassword(stringCaptor.capture(), stringCaptor.capture())).thenReturn(defaultUserModel);
    AuthRequest authRequest = new AuthRequest(defaultUserModel.getUserName(), defaultUserModel.getPassword());

    var response = instance.authenticateUser(authRequest);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Success", response.getBody().getMessage());
    assertNotNull(response.getBody().getData().getToken());
    assertEquals("AUTHORIZED", response.getBody().getData().getStatus());
    List<String> allValuesCaptured = stringCaptor.getAllValues();
    assertEquals(authRequest.getUserName(), allValuesCaptured.get(0));
    assertEquals(authRequest.getPassword(), allValuesCaptured.get(1));
  }

  @Test
  void authenticateUser_returnsGeneralFailureResponse_whenExceptionOccurs() {
    AuthRequest authRequest = new AuthRequest(defaultUserModel.getUserName(), defaultUserModel.getPassword());
    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.when(userRepository.findByUserNameAndPassword(stringCaptor.capture(), stringCaptor.capture())).thenThrow(RuntimeException.class);
    var generalFailureResponse = ResponseEntity.ok(new BaseResponse<>(
              -3,
              "Bad gateway - something bad happened contact support",
              null
      ));
    var response = instance.authenticateUser(authRequest);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(new ReflectionEquals(generalFailureResponse).matches(response));
    List<String> allValuesCaptured = stringCaptor.getAllValues();
    assertEquals(authRequest.getUserName(), allValuesCaptured.get(0));
    assertEquals(authRequest.getPassword(), allValuesCaptured.get(1));
  }

  @Test
  void renewToken_returnsForbidden_forInvalidToken() {
    HttpHeaders invalidTokenHeader = new HttpHeaders();
    invalidTokenHeader.set(HttpHeaders.AUTHORIZATION, "Bearer 123");
    var invalidResponse = ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(new BaseResponse(-802, "Unauthorized or expired token", null));

    var response = instance.renewToken(invalidTokenHeader);

    assertNotNull(response);
    assertTrue(new ReflectionEquals(invalidResponse).matches(response));
  }

  @Test
  void renewToken_returnsSuccess_forValidToken(){
    String authToken = authService.generateAuthToken(defaultUserModel);
    authService.updateLastToken(defaultUserModel, authToken);
    HttpHeaders validTokenHeader = new HttpHeaders();
    validTokenHeader.set(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
    TokenModel validToken = new TokenModel(42L, defaultUserModel, authToken, new Date(),
            new Date(now().plus(30, MINUTES).toEpochSecond(ZoneOffset.of("-03:00")) * 1000));
    Mockito.when(tokenRepository.findByToken(authToken)).thenReturn(Optional.of(validToken));
    var response = instance.renewToken(validTokenHeader);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getData());
    assertEquals(OK, response.getStatusCode());
    assertEquals(0, response.getBody().getErrorCode());
    assertEquals("Success", response.getBody().getMessage());
    assertEquals("AUTHORIZED", response.getBody().getData().getStatus());

  }
}