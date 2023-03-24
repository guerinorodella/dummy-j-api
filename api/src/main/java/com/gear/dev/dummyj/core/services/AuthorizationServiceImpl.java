package com.gear.dev.dummyj.core.services;

import com.gear.dev.dummyj.core.model.TokenModel;
import com.gear.dev.dummyj.core.model.UserModel;
import com.gear.dev.dummyj.core.repository.TokenRepository;
import com.gear.dev.dummyj.core.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

//  @TODO move to an more generic class or Enum maybe? TBD
  public static final int BLOCKED = -2;
  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private Map<String, UserModel> authorizedUserMap;

  @Autowired
  public AuthorizationServiceImpl(UserRepository repository, TokenRepository tokenRepository) {
    this.repository = repository;
    this.tokenRepository = tokenRepository;
    authorizedUserMap = new HashMap<>();
  }

  @Override
  public UserModel findUser(String userName, String password) {
    return repository.findByUserNameAndPassword(userName, password);
  }

  @Override
  public String generateAuthToken(UserModel user) {
    if (user == null || isUserBlocked(user)) {
      throw new IllegalArgumentException("User blocked or is null");
    }
    return Jwts.builder()
            .claim("name", user.getUserName())
            .claim("email", user.getEmail())
            .setId(String.valueOf(user.getId()))
            .setIssuedAt(new Date())
            .setExpiration(Date.from(now().plus(1, HOURS)))
            .compact();
  }

  @Override
  public void updateLastToken(UserModel user, String authToken) {

    var token = new TokenModel();
    token.setToken(authToken);
    token.setUser(user);
    token.setCreatedTime(new Date());
    token.setExpiryTime(Date.from(now().plus(1, HOURS)));
    tokenRepository.save(token);
    authorizedUserMap.put(authToken, user);
    // @TODO handle exceptions
  }

  @Override
  public boolean isUserAuthenticated(String authToken) {
    if (!authorizedUserMap.containsKey(authToken)) {
      return false;
    }
    var optionalToken = tokenRepository.findByToken(authToken);
    if (optionalToken.isEmpty()) {
      return false;
    }
    var expiryTime = optionalToken.get().getExpiryTime().toInstant();
    return now().isBefore(expiryTime);
  }

  @Override
  public boolean isUserBlocked(UserModel user) {
    return BLOCKED == user.getStatus();
  }

  @Override
  public String renewToken(String lastToken) {
    var user = authorizedUserMap.get(lastToken);
    String newToken = generateAuthToken(user);
    updateLastToken(user, newToken);
    return newToken;
  }
}
