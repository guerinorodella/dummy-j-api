package com.gear.dev.dummyj.app.contoller.product.category;

import com.gear.dev.dummyj.app.contoller.BaseResponse;
import com.gear.dev.dummyj.app.validations.SessionValidator;
import com.gear.dev.dummyj.core.model.ProductCategory;
import com.gear.dev.dummyj.core.repository.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/product/category")
public class ProductCategoryController {

  private final ProductCategoryRepository repository;
  private final SessionValidator sessionValidator;

  @Autowired
  public ProductCategoryController(ProductCategoryRepository repository,
                                   SessionValidator sessionValidator) {
    this.repository = repository;
    this.sessionValidator = sessionValidator;
  }

  @GetMapping
  public ResponseEntity<BaseResponse<Object>> getAllProductCategory(@RequestHeader HttpHeaders headers) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      return ResponseEntity.ok().body(
              new BaseResponse<>(0, "Success", repository.findAll()));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - " + ex.getMessage(), null));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Object> getProductCategory(@RequestHeader HttpHeaders headers,
                                                   @PathVariable Long id) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      return ResponseEntity.ok().body(
              new BaseResponse<>(0, "Success", repository.findById(id).orElseGet(null)));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - " + ex.getMessage(), null));
    }
  }

  //@TODO - check the category ID if wont crash DB
  @PostMapping("/add")
  public ResponseEntity<Object> addProductCategory(@RequestHeader HttpHeaders headers,
                                                   @RequestBody ProductCategory category) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      var createdCategory = repository.save(category);
      return ResponseEntity.created(ServletUriComponentsBuilder
                      .fromCurrentRequest()
                      .replacePath("/product/category")
                      .path("/{id}")
                      .buildAndExpand(createdCategory.getId())
                      .toUri())
              .body(BaseResponse.success(createdCategory));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - " + ex.getMessage(), null));
    }
  }

  @PutMapping("/{id}")
  @PatchMapping("/{id}")
  public ResponseEntity<Object> updateProductCategory(@RequestHeader HttpHeaders headers,
                                                      @PathVariable Long id,
                                                      @RequestBody ProductCategory category) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {

      var optionalResponse = repository.findById(id);
      if (optionalResponse.isPresent()) {
        var savedCategory = optionalResponse.get();
        savedCategory.setDescription(category.getDescription());
        return ResponseEntity.ok().body(repository.save(savedCategory));
      }

      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new BaseResponse<>(-4, "No category found with provided ID", null));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(
              new BaseResponse<>(-900, "API ERROR - " + ex.getMessage(), null));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteProductCategory(@RequestHeader HttpHeaders headers,
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
