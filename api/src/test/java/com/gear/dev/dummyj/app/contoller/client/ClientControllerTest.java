package com.gear.dev.dummyj.app.contoller.client;

import com.gear.dev.dummyj.app.contoller.BaseResponse;
import com.gear.dev.dummyj.app.contoller.StandardTestForControllers;
import com.gear.dev.dummyj.core.model.ClientModel;
import com.gear.dev.dummyj.core.repository.ClientRepository;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.*;

@DataJpaTest
@ActiveProfiles("test")
class ClientControllerTest extends StandardTestForControllers {


  @Mock
  private ClientRepository repository;
  private ClientController instance;
  private ClientRequest defaultClientRequest;
  private ClientModel defaultClientModel;

  @BeforeEach
  void setUp() {
    instance = new ClientController(sessionValidator, repository);
    defaultClientModel = new ClientModel(43L, "Jhon Foo", "5514988127573", "foo_jhson@mail.com", "cpei,mtm_id", now(), 1);
    defaultClientRequest = new ClientRequest("Joshua", "66987124455",
            "joshua.jho@mail.com", "DOC_ID_1234", 0);
  }

  @Test
  void getAllClients_returnsSuccess_forValidSessionHeader() {
    var stringCaptor = ArgumentCaptor.forClass(String.class);
    when(authService.isUserAuthenticated(stringCaptor.capture())).thenReturn(true);
    List<ClientModel> allClientList = Arrays.asList(
            new ClientModel(43L, "Jhon Foo", "5514988127573", "foo_jhson@mail.com", "cpei,mtm_id", now(), 1),
            new ClientModel(43L, "Jhon Due", "5514988127573", "foo_jhson@mail.com", "cpei,mtm_id", now(), 1),
            new ClientModel(43L, "Foo Jhon", "5514988127573", "foo_jhson@mail.com", "cpei,mtm_id", now(), 1));
    var expectedResponseBody = new BaseResponse<List<ClientModel>>(0, "Success", allClientList);
    when(repository.findAll()).thenReturn(allClientList);

    var response = instance.getAllClients(defaultValidHeader);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getData());
    assertEquals(OK, response.getStatusCode());
    assertTrue(new ReflectionEquals(expectedResponseBody).matches(response.getBody()));
    assertEquals("123456", stringCaptor.getValue());
  }

  @Test
  void getAllClients_returnsForbidden_forInvalidSession() {
    var result = instance.getAllClients(new HttpHeaders());
    assertEquals(FORBIDDEN, result.getStatusCode());
  }

  @Test
  void getClientsById_returnsSuccess_forValidSessionHeader_andValidUserId() {
    var stringCaptor = ArgumentCaptor.forClass(String.class);

    var expectedResponseBody = new BaseResponse<>(0, "Success", defaultClientModel);
    when(authService.isUserAuthenticated(stringCaptor.capture())).thenReturn(true);
    when(repository.findById(defaultClientModel.getId())).thenReturn(Optional.of(defaultClientModel));

    var response = instance.getClientById(defaultValidHeader, defaultClientModel.getId());

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getData());
    assertEquals(OK, response.getStatusCode());
    assertTrue(new ReflectionEquals(expectedResponseBody).matches(response.getBody()));
    assertEquals(DEFAULT_VALID_SESSION, stringCaptor.getValue());
  }

  @Test
  void getClientById_returns404NotFound_forInvalidClientId() {
    var stringCaptor = ArgumentCaptor.forClass(String.class);
    when(authService.isUserAuthenticated(stringCaptor.capture())).thenReturn(true);
    when(repository.findById(1L)).thenReturn(Optional.empty());

    var response = instance.getClientById(defaultValidHeader, 1L);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getClientById_returnsForbidden_forInvalidSession() {
    var result = instance.getClientById(new HttpHeaders(), 1L);
    assertEquals(FORBIDDEN, result.getStatusCode());
  }

  @Test
  void addClient_returnsCreated_forSuccessClientAdded() {
    var stringCaptor = ArgumentCaptor.forClass(String.class);
    when(authService.isUserAuthenticated(stringCaptor.capture())).thenReturn(true);
    var clientCaptor = ArgumentCaptor.forClass(ClientModel.class);
    when(repository.save(clientCaptor.capture())).thenReturn(defaultClientModel);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

    var response = instance.addClient(defaultValidHeader, defaultClientRequest);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getData());
    assertNotNull(response.getHeaders());
    assertTrue(response.getHeaders().containsKey(LOCATION));
    var insertedClient = clientCaptor.getValue();
    assertTrue(response.getHeaders().get(LOCATION).get(0).matches(".*" + defaultClientModel.getId()));
    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(insertedClient);
    assertNotNull(insertedClient.getCreatedDate());
    assertEquals(-1, insertedClient.getStatus()); // @TODO refactor with constant
    assertTrue(new ReflectionEquals(defaultClientModel).matches(response.getBody().getData()));
    verify(repository, atLeastOnce()).save(any(ClientModel.class));

  }

  @Test
  void addClient_returnsForbidden_forInvalidSession() {
    var result = instance.addClient(new HttpHeaders(), defaultClientRequest);
    assertEquals(FORBIDDEN, result.getStatusCode());
  }

  @Test
  void updateClient_returnsForbidden_forInvalidSession() {
    var result = instance.addClient(new HttpHeaders(), defaultClientRequest);
    assertEquals(FORBIDDEN, result.getStatusCode());
  }

  @Test
  void updateClient_returns404NotFound_forInvalidClientId() {
    var sessionCaptor = ArgumentCaptor.forClass(String.class);
    when(authService.isUserAuthenticated(sessionCaptor.capture())).thenReturn(true);
    when(repository.findById(1L)).thenReturn(Optional.empty());
    var expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new BaseResponse<>(-4, "No user found with provided ID", null));

    var response = instance.updateClient(defaultValidHeader, 1L, defaultClientRequest);

    assertNotNull(response);
    assertTrue(new ReflectionEquals(expectedResponse).matches(response));
    assertEquals(NOT_FOUND, response.getStatusCode());
    assertEquals(DEFAULT_VALID_SESSION, sessionCaptor.getValue());
    verify(repository, atLeastOnce()).findById(1L);
    verify(repository, never()).save(any());
  }

  @Test
  void updateClient_returnsSuccess() {
    var sessionCaptor = ArgumentCaptor.forClass(String.class);
    var clientCaptor = ArgumentCaptor.forClass(ClientModel.class);
    when(authService.isUserAuthenticated(sessionCaptor.capture())).thenReturn(true);
    when(repository.findById(defaultClientModel.getId())).thenReturn(Optional.of(defaultClientModel));
    when(repository.save(clientCaptor.capture())).thenReturn(defaultClientModel);

    var response = instance.updateClient(defaultValidHeader, defaultClientModel.getId(), defaultClientRequest);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getData());
    verify(repository, atLeastOnce()).findById(defaultClientModel.getId());
    verify(repository, atLeastOnce()).save(clientCaptor.getValue());
    var insertedClient = clientCaptor.getValue();
    assertEquals(defaultClientRequest.getEmailAddress(), insertedClient.getEmailAddress());
    assertEquals(defaultClientRequest.getDocumentId(), insertedClient.getDocumentId());
    assertEquals(defaultClientRequest.getName(), insertedClient.getName());
    assertEquals(defaultClientRequest.getStatus(), insertedClient.getStatus());
  }

  @Test
  void deleteClient_returnsForbidden_forInvalidSession() {
    var result = instance.deleteClient(new HttpHeaders(), 42L);
    assertEquals(FORBIDDEN, result.getStatusCode());
  }

  @Test
  void deleteClient_returnsSuccess() {
    var sessionCaptor = ArgumentCaptor.forClass(String.class);
    when(authService.isUserAuthenticated(sessionCaptor.capture())).thenReturn(true);
    var expectedResponse = ResponseEntity.ok().body(new BaseResponse<>(0, "Success", null));

    var response = instance.deleteClient(defaultValidHeader, 42L);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(DEFAULT_VALID_SESSION, sessionCaptor.getValue());
    assertTrue(new ReflectionEquals(expectedResponse).matches(response));
    assertEquals(OK, response.getStatusCode());
    verify(repository, atLeastOnce()).deleteById(42L);
    verifyNoMoreInteractions(repository);
  }
}