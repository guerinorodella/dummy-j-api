package com.gear.dev.dummyj.app.contoller.user;

import com.gear.dev.dummyj.app.contoller.BaseResponse;
import com.gear.dev.dummyj.app.validations.SessionValidator;
import com.gear.dev.dummyj.core.model.UserModel;
import com.gear.dev.dummyj.core.repository.UserRepository;
import com.gear.dev.dummyj.core.services.AuthorizationService;
import com.gear.dev.dummyj.core.services.AuthorizationServiceImpl;
import org.apache.catalina.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

@DataJpaTest
@ActiveProfiles("test")
class UserControllerTest {

  @Mock
  private UserRepository repository;
  @Mock
  private AuthorizationService authService;
  private SessionValidator sessionValidator;
  private UserController instance;
  private HttpHeaders defaultValidHeader;
  private ResponseEntity<BaseResponse> forbiddenResponse;


  @BeforeEach
  void setUp() {
    defaultValidHeader = new HttpHeaders();
    defaultValidHeader.set(HttpHeaders.AUTHORIZATION, "Bearer 123456");
    forbiddenResponse = ResponseEntity.status(FORBIDDEN).body(new BaseResponse(-802,
            "Unauthorized or expired token", null));
    sessionValidator = new SessionValidator(authService);
    instance = new UserController(repository, sessionValidator);
  }

  @Test
  public void forbiddenResponse_whenSessionId_notProvided() throws Exception {
    var result = instance.getUser(new HttpHeaders(), 1L);
    assertEquals(FORBIDDEN, result.getStatusCode());
  }

  @Test
  public void forbiddenResponse_forInvalidSessionId() throws Exception {
    var header = new HttpHeaders();
    header.set("Authorization", "Bearer 1234568789799");
    var result = instance.getUser(header, 1L);
    assertEquals(FORBIDDEN, result.getStatusCode());
  }

  @Test
  public void ok_forValidSessionId() throws Exception {
    var expectedUser = new UserModel(1L, "foo", "123456", "foo@mail.com", now(), 0);
    var header = new HttpHeaders();
    String validSessionId = "111111111111";
    header.set("Authorization", "Bearer " + validSessionId);
    when(authService.isUserAuthenticated(validSessionId)).thenReturn(true);
    when(repository.findById(ArgumentMatchers.eq(1L))).thenReturn(Optional.of(expectedUser));

    var result = instance.getUser(header, 1L);
    assertEquals(OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertNotNull(result.getBody().getData());
    assertInstanceOf(UserModel.class, result.getBody().getData());
    var resultUser = (UserModel) result.getBody().getData();
    assertTrue(new ReflectionEquals(expectedUser).matches(resultUser));
  }

  @Test
  void getAllUsers_returnsForbidden_whenTokenIsInvalid() {
    defaultValidHeader.set(HttpHeaders.AUTHORIZATION, "Bearer INVALID");
    var stringCaptor = ArgumentCaptor.forClass(String.class);
    when(authService.isUserAuthenticated(stringCaptor.capture())).thenReturn(false);

    var response = instance.getAllUsers(defaultValidHeader);
    assertNotNull(response);
    assertTrue(new ReflectionEquals(forbiddenResponse).matches(response));
    assertEquals("INVALID", stringCaptor.getValue());
  }

  @Test
  void getAllUsers_successResponse() {
    var stringCaptor = ArgumentCaptor.forClass(String.class);
    when(authService.isUserAuthenticated(stringCaptor.capture())).thenReturn(true);
    List<UserModel> allUserList = Arrays.asList(
            new UserModel(43L, "foo_foo", "123456", "foo_foo@mail.com", now(), 1),
            new UserModel(41L, "f_foo41", "12345641", "foo_f41@mail.com", now(), 1));
    var expectedResponseBody = new BaseResponse<>(0, "Success", allUserList);
    when(repository.findAll()).thenReturn(allUserList);

    var response = instance.getAllUsers(defaultValidHeader);
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getData());
    assertEquals(OK, response.getStatusCode());
    assertTrue(new ReflectionEquals(expectedResponseBody).matches(response.getBody()));
    assertEquals("123456", stringCaptor.getValue());

  }

  @Test
  void getAllUsers_returnsServerErrorResponse_forExceptions(){
    var stringCaptor = ArgumentCaptor.forClass(String.class);
    when(authService.isUserAuthenticated(stringCaptor.capture())).thenReturn(true);
    when(repository.findAll()).thenThrow(new RuntimeException("Could not connect to DB"));
    var internalServerErrorResponse = ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - Could not connect to DB", null));

    var response = instance.getAllUsers(defaultValidHeader);
    assertNotNull(response);
    assertTrue(new ReflectionEquals(internalServerErrorResponse).matches(response));
    assertEquals("123456", stringCaptor.getValue());
  }
}