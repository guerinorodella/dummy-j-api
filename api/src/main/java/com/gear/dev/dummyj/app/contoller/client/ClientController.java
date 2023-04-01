package com.gear.dev.dummyj.app.contoller.client;

import com.gear.dev.dummyj.app.contoller.BaseResponse;
import com.gear.dev.dummyj.app.contoller.user.UserRequest;
import com.gear.dev.dummyj.app.validations.SessionValidator;
import com.gear.dev.dummyj.core.model.ClientModel;
import com.gear.dev.dummyj.core.repository.ClientRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static java.time.LocalDateTime.now;

@RestController("/client")
public class ClientController {

  private final SessionValidator sessionValidator;
  private final ClientRepository repository;

  @Autowired
  public ClientController(SessionValidator sessionValidator, ClientRepository repository) {
    this.sessionValidator = sessionValidator;
    this.repository = repository;
  }

  @GetMapping
  public ResponseEntity<BaseResponse<Object>> getAllClients(@RequestHeader HttpHeaders headers) {
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
  public ResponseEntity<BaseResponse<Object>> getClientById(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      var optClientResult = repository.findById(id);
      return optClientResult.<ResponseEntity<BaseResponse<Object>>>map(clientModel ->
                      ResponseEntity.ok(new BaseResponse<>(0, "Success", clientModel)))
              .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                      .body(new BaseResponse<>(-404, "Failure", "Client not found with " +
                              "provided ID: " + id)));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - " + ex.getMessage(), null));
    }
  }

  @PostMapping("/add")
  public ResponseEntity<BaseResponse<Object>> addClient(@RequestHeader HttpHeaders headers,
                                                      @RequestBody ClientRequest clientRequest) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      var clientModel = new ClientModel();
      BeanUtils.copyProperties(clientRequest, clientModel);
      clientModel.setCreatedDate(now());
      clientModel.setStatus(-1);// @TODO what's -1 ? Create a constant enum for it
      var createdUser = repository.save(clientModel);
      return ResponseEntity.created(ServletUriComponentsBuilder
                      .fromCurrentRequest()
                      .replacePath("/client")
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
  public ResponseEntity<BaseResponse<Object>> updateClient(@RequestHeader HttpHeaders headers,
                                                         @PathVariable Long id,
                                                         @RequestBody ClientRequest clientRequest) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      var optionalClient = repository.findById(id);
      if (optionalClient.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BaseResponse<>(-4, "No user found with provided ID", null));
      }

      var clientModel = optionalClient.get();
      clientModel.setDocumentId(clientRequest.getDocumentId());
      clientModel.setEmailAddress(clientRequest.getEmailAddress());
      clientModel.setPhoneNumber(clientRequest.getPhoneNumber());
      clientModel.setName(clientRequest.getName());
      clientModel.setStatus(clientRequest.getStatus());

      return ResponseEntity.ok()
              .body(new BaseResponse<>(0, "Success", repository.save(clientModel))); // @TODO create a clientResponse Json model instead of DB model ???
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - " + ex.getMessage(), null));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<BaseResponse<Object>> deleteClient(@RequestHeader HttpHeaders headers,
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
