package com.gear.dev.dummyj.core.services;

import com.gear.dev.dummyj.core.model.TokenModel;
import com.gear.dev.dummyj.core.model.UserModel;
import com.gear.dev.dummyj.core.repository.TokenRepository;
import com.gear.dev.dummyj.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static com.gear.dev.dummyj.core.services.AuthorizationServiceImpl.BLOCKED;
import static java.lang.System.currentTimeMillis;
import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthorizationServiceImplTest {

  private UserRepository userRepository;
  private TokenRepository tokenRepository;
  private AuthorizationService instance;
  private UserModel defaultUserModel;

  @BeforeEach
  void setUp() {
    userRepository = Mockito.mock(UserRepository.class);
    tokenRepository = Mockito.mock(TokenRepository.class);
    defaultUserModel = new UserModel(42L, "foo", "123456", "foo@mail.com", now(), 0);

    instance = new AuthorizationServiceImpl(userRepository, tokenRepository);
  }

  @Test
  void mustFindUser_withUserNameAndPassword() {
    String name = "jhon.jhon";
    String password = "123456";
    when(userRepository.findByUserNameAndPassword(eq(name), eq(password))).thenReturn(defaultUserModel);
    var argumentCaptor = ArgumentCaptor.forClass(String.class);

    var result = instance.findUser(name, password);

    assertNotNull(result);
    assertTrue(new ReflectionEquals(defaultUserModel).matches(result));
    verify(userRepository, atLeastOnce()).findByUserNameAndPassword(argumentCaptor.capture(), argumentCaptor.capture());
    assertEquals(name, argumentCaptor.getAllValues().get(0));
    assertEquals(password, argumentCaptor.getAllValues().get(1));
  }

  @Test
  void generateAuthToken_throwsIllegalArgumentException_forNullUser() {
    assertThrows(IllegalArgumentException.class,
            () -> instance.generateAuthToken(null));
    verifyNoInteractions(tokenRepository);
    verifyNoInteractions(userRepository);
  }

  @Test
  void generateAuthToken_throwsIllegalArgumentException_forBlockedUser() {
    defaultUserModel.setStatus(BLOCKED);
    assertThrows(IllegalArgumentException.class,
            () -> instance.generateAuthToken(defaultUserModel));
    verifyNoInteractions(tokenRepository);
    verifyNoInteractions(userRepository);
  }

  @Test
  void generateAuthToken_returnsValidToken_forValidUser() {
//    @TODO how could this be improved? Validation of generated token
    var response = instance.generateAuthToken(defaultUserModel);
    assertNotNull(response);
    verifyNoInteractions(tokenRepository);
    verifyNoInteractions(userRepository);
  }

  @Test
  void updateLastToken() {
    String authToken = "123456";
    var tokenCaptor = ArgumentCaptor.forClass(TokenModel.class);

    instance.updateLastToken(defaultUserModel, authToken);

    verify(tokenRepository, atLeastOnce()).save(tokenCaptor.capture());
    var insertedToken = tokenCaptor.getValue();
    assertNotNull(insertedToken);
    assertEquals(authToken, insertedToken.getToken());
    assertEquals(defaultUserModel, insertedToken.getUser());
    assertEquals(authToken, insertedToken.getToken());
  }

  @Test
  void userIsAuthenticated_returnsFalse_whenItsNotPresentOnAuthMap() {
    boolean response = instance.isUserAuthenticated("123456");

    assertFalse(response);
    verifyNoInteractions(tokenRepository, userRepository);

  }

  @Test
  void userIsAuthenticated_returnsFalseWhenNotFoundOnDB() {
    String authToken = "123456";
    instance.updateLastToken(defaultUserModel, authToken); // this call will populate the authorizeMap in memory;
    when(tokenRepository.findByToken(authToken)).thenReturn(Optional.empty());
    var tokenCaptor = ArgumentCaptor.forClass(String.class);

    var response = instance.isUserAuthenticated(authToken);

    assertFalse(response);
    verify(tokenRepository, atLeastOnce()).findByToken(tokenCaptor.capture());
    assertEquals(authToken, tokenCaptor.getValue());
  }

  @Test
  void userIsAuthenticated_returnsFalse_exceedsExpiryTime() {
    String authToken = "123456";
    instance.updateLastToken(defaultUserModel, authToken); // this call will populate the authorizeMap in memory;
    TokenModel expiredToken = new TokenModel(42L, defaultUserModel, authToken, new Date(currentTimeMillis()),
            new Date(1L));
    when(tokenRepository.findByToken(authToken)).thenReturn(Optional.of(expiredToken));
    var tokenCaptor = ArgumentCaptor.forClass(String.class);

    var response = instance.isUserAuthenticated(authToken);

    assertFalse(response);
    verify(tokenRepository, atLeastOnce()).findByToken(tokenCaptor.capture());
    assertEquals(authToken, tokenCaptor.getValue());
  }

  @Test
  void userIsAuthenticated_returnsTrue_forValidToken() {
    String authToken = "123456";
    instance.updateLastToken(defaultUserModel, authToken); // this call will populate the authorizeMap in memory;
    Date nowPlus30Minutes = new Date(Instant.now().plus(30, MINUTES.toChronoUnit()).getEpochSecond() * 1000);
    TokenModel expiredToken = new TokenModel(42L, defaultUserModel, authToken, new Date(currentTimeMillis()),
            nowPlus30Minutes);
    when(tokenRepository.findByToken(authToken)).thenReturn(Optional.of(expiredToken));
    var tokenCaptor = ArgumentCaptor.forClass(String.class);

    var response = instance.isUserAuthenticated(authToken);

    assertTrue(response);
    verify(tokenRepository, atLeastOnce()).findByToken(tokenCaptor.capture());
    assertEquals(authToken, tokenCaptor.getValue());
  }
}