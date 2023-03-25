package com.gear.dev.dummyj.app.contoller.user;

import com.gear.dev.dummyj.app.contoller.BaseResponse;
import com.gear.dev.dummyj.app.validations.SessionValidator;
import com.gear.dev.dummyj.core.model.UserModel;
import com.gear.dev.dummyj.core.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@RestController
@RequestMapping("/user")
public class UserController {

  private final UserRepository repository;
  private final SessionValidator sessionValidator;

  @Autowired
  public UserController(UserRepository userRepository, SessionValidator sessionValidator) {
    this.repository = userRepository;
    this.sessionValidator = sessionValidator;
  }

  @GetMapping()
  public ResponseEntity<BaseResponse<Object>> getAllUsers(@RequestHeader HttpHeaders headers) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      var resultList = repository.findAll();
      return ResponseEntity.ok(new BaseResponse<>(
              0, "Success", resultList));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - " + ex.getMessage(), null));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseResponse<Object>> getUser(@RequestHeader HttpHeaders headers,
                                                      @PathVariable Long userId) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      var optionalResult = repository.findById(userId);
      if (optionalResult.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BaseResponse<>(-404, "Failure", "User not found with provided ID: " + userId));
      }
      return ResponseEntity.ok()
              .body(BaseResponse.success(optionalResult.get()));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - " + ex.getMessage(), null));
    }
  }

  @PostMapping("/add")
  public ResponseEntity<BaseResponse<Object>> addUser(@RequestHeader HttpHeaders headers,
                                                      @RequestBody UserRequest userRequest) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      var userModel = new UserModel();
      BeanUtils.copyProperties(userRequest, userModel);
      userModel.setCreatedTime(now());
      userModel.setStatus(-1);
      var createdUser = repository.save(userModel);
      return ResponseEntity.created(ServletUriComponentsBuilder
                      .fromCurrentRequest()
                      .replacePath("/user")
                      .path("/{id}")
                      .buildAndExpand(createdUser.getId())
                      .toUri())
              .body(new BaseResponse<>(0, "Success", createdUser));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - " + ex.getMessage(), null));
    }
  }

  @PutMapping("/{id}")
  @PatchMapping("/{id}")
  public ResponseEntity<BaseResponse<Object>> updateUser(@RequestHeader HttpHeaders headers,
                                                         @PathVariable Long id,
                                                         @RequestBody UserRequest userRequest) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      var optionalUser = repository.findById(id);
      if (optionalUser.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BaseResponse<>(-4, "No user found with provided ID", null));
      }

      var userModel = optionalUser.get();
      userModel.setUserName(userRequest.getUserName());
      userModel.setPassword(userRequest.getPassword());
      userModel.setEmail(userRequest.getEmail());
      return ResponseEntity.ok()
              .body(new BaseResponse<>(0, "Success", repository.save(userModel)));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - " + ex.getMessage(), null));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<BaseResponse<Object>> deleteUser(@RequestHeader HttpHeaders headers,
                                                         @PathVariable Long id) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      repository.deleteById(id);
      return ResponseEntity.ok().body(new BaseResponse<>(0, "Success", null));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - " + ex.getMessage(), null));
    }

  }

}
