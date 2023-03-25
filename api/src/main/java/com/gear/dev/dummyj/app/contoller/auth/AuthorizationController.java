package com.gear.dev.dummyj.app.contoller.auth;

import com.gear.dev.dummyj.app.contoller.BaseResponse;
import com.gear.dev.dummyj.app.validations.SessionValidator;
import com.gear.dev.dummyj.core.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/auth")
public class AuthorizationController {

  private final AuthorizationService authService;
  private final SessionValidator sessionValidator;

  @Autowired
  public AuthorizationController(AuthorizationService authService, SessionValidator sessionValidator) {
    this.authService = authService;
    this.sessionValidator = sessionValidator;
  }

  @PostMapping
  public ResponseEntity<BaseResponse<AuthResponse>> authenticateUser(@RequestBody AuthRequest authRequest) {

    try {
      var user = authService.findUser(authRequest.getUserName(), authRequest.getPassword());
      if (user == null) {
        return ResponseEntity.status(NOT_FOUND)
                .body(new BaseResponse<AuthResponse>(
                        -1,
                        "User not found",
                        new AuthResponse(null, "NOT_FOUND")));
      }
      if (authService.isUserBlocked(user)) {
        return ResponseEntity.ok(new BaseResponse<AuthResponse>(
                -2,
                "User is blocked",
                new AuthResponse(null, "BLOCKED")));
      }
      String authToken = authService.generateAuthToken(user);
      authService.updateLastToken(user, authToken);
      return ResponseEntity.ok(new BaseResponse<>(
              0,
              "Success",
              new AuthResponse(authToken, "AUTHORIZED")
      ));

    } catch (Exception ex) {
      // @TODO add logger
      return ResponseEntity.ok(new BaseResponse<>(
              -3,
              "Bad gateway - something bad happened contact support",
              null));
    }


  }

  @GetMapping("/renew-token")
  public ResponseEntity<BaseResponse<AuthResponse>> renewToken(@RequestHeader HttpHeaders header) {
    var validateResponse = sessionValidator.validateSession(header);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }
    String lastToken = (String) validateResponse.getData();
    String newToken = authService.renewToken(lastToken);
    return ResponseEntity.ok(new BaseResponse<>(
            0,
            "Success",
            new AuthResponse(newToken, "AUTHORIZED")));
  }

}
