package com.gear.dev.dummyj.app.contoller;

import com.gear.dev.dummyj.app.validations.SessionValidator;
import com.gear.dev.dummyj.core.services.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@DataJpaTest
@ActiveProfiles("test")
public class StandardTestForControllers {

  protected static final String DEFAULT_VALID_SESSION = "123456";

  @Mock
  protected AuthorizationService authService;
  protected SessionValidator sessionValidator;
  protected HttpHeaders defaultValidHeader;
  protected ResponseEntity<BaseResponse> forbiddenResponse;

  @BeforeEach
  void setUpDefaults() {
    defaultValidHeader = new HttpHeaders();
    defaultValidHeader.set(HttpHeaders.AUTHORIZATION, "Bearer " + DEFAULT_VALID_SESSION);
    forbiddenResponse = ResponseEntity.status(FORBIDDEN).body(new BaseResponse(-802,
            "Unauthorized or expired token", null));
    sessionValidator = new SessionValidator(authService);
  }
}
