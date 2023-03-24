package com.gear.dev.dummyj.app.validations;

import com.gear.dev.dummyj.app.contoller.BaseResponse;
import com.gear.dev.dummyj.core.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class SessionValidator {

  private final AuthorizationService authService;

  @Autowired
  public SessionValidator(AuthorizationService authService) {
    this.authService = authService;
  }

  public BaseResponse validateSession(HttpHeaders headers) {
    if (!headers.containsKey(AUTHORIZATION)) {
      return new BaseResponse(-800, "Missing Authorization header", null);
    }
    var bearerList = headers.get(HttpHeaders.AUTHORIZATION);
    if (bearerList == null || bearerList.isEmpty()) {
      return new BaseResponse(-800, "Missing Authorization header", null);
    }

    try {

      String token = removeBearerWordFromBearerToken(bearerList.get(0));
      boolean authenticated = authService.isUserAuthenticated(token);
      if (authenticated) {
        return new BaseResponse(0, "Success", token);
      }

      return new BaseResponse(-802, "Unauthorized or expired token", null);

    } catch (RuntimeException ex) {
      return new BaseResponse(-801, "Invalid bearer received", null);
    }
  }

  //    sample: "Bearer 123456479878946516541684" -> will return "123456479878946516541684"
  private String removeBearerWordFromBearerToken(String bearerToken) {
    return bearerToken.substring("Bearer ".length());
  }
}
