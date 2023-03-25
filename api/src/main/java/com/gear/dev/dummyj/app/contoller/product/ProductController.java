package com.gear.dev.dummyj.app.contoller.product;

import com.gear.dev.dummyj.app.contoller.BaseResponse;
import com.gear.dev.dummyj.app.validations.SessionValidator;
import com.gear.dev.dummyj.core.model.Product;
import com.gear.dev.dummyj.core.repository.ProductCategoryRepository;
import com.gear.dev.dummyj.core.repository.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/product")
public class ProductController {

  private final ProductRepository repository;
  private final ProductCategoryRepository categoryRepository;
  private final SessionValidator sessionValidator;

  @Autowired
  public ProductController(ProductRepository repository,
                           ProductCategoryRepository categoryRepository,
                           SessionValidator sessionValidator) {
    this.repository = repository;
    this.categoryRepository = categoryRepository;
    this.sessionValidator = sessionValidator;
  }

  @GetMapping
  public ResponseEntity<BaseResponse<ProductResponse>> getAllProducts(@RequestHeader HttpHeaders headers) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {

      List<Product> productList = repository.findAll();
      return ResponseEntity.ok(new BaseResponse(0, "Success",
              new ProductResponse(
                      productList,
                      productList.size(),
                      0,
                      productList.size() > 0 ? productList.size() / 2 : 0
              )));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
              .body(new BaseResponse<>(-999, e.getMessage(), null));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseResponse<Product>> getProduct(@RequestHeader HttpHeaders headers,
                                                          @PathVariable(value = "id") Long id) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      var optionalProduct = repository.findById(id);
      if (optionalProduct.isPresent()) {
        var product = optionalProduct.get();
        return ResponseEntity.ok(new BaseResponse<Product>(
                0,
                "Success",
                product
        ));
      }
      return ResponseEntity.ok(null);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
              .body(null);
    }
  }

  @PostMapping("/add")
  public ResponseEntity<BaseResponse<Product>> addProduct(@RequestHeader HttpHeaders headers,
                                                          @RequestBody ProductRequest productRequest) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {

      var category = categoryRepository.findById(productRequest.getCategory());
      if (category.isEmpty()) {
        return ResponseEntity.ok(new BaseResponse<>(
                -1,
                "Invalid Category provided",
                null
        ));
      }

      var product = new Product();
      BeanUtils.copyProperties(productRequest, product);
      var createdProduct = repository.save(product);

      return ResponseEntity.created(ServletUriComponentsBuilder
                      .fromCurrentRequest()
                      .replacePath("/product")
                      .path("/{id}")
                      .buildAndExpand(createdProduct.getId())
                      .toUri())
              .body(BaseResponse.success(createdProduct));
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @PutMapping("/{id}")
  @PatchMapping("/{id}")
  public ResponseEntity<BaseResponse> updateProduct(@RequestHeader HttpHeaders headers,
                                                    @PathVariable Long id,
                                                    @RequestBody ProductRequest incomingProduct) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      var optionalProduct = repository.findById(id);
      if (optionalProduct.isEmpty()) {
        return ResponseEntity.status(NOT_FOUND)
                .body(new BaseResponse(
                        -2,
                        "Nothing found with provided ID",
                        new Product()
                ));
      }
      var category = categoryRepository.findById(incomingProduct.getCategory());
      if (category.isEmpty()) {
        return ResponseEntity.ok(new BaseResponse<>(
                -1,
                "Invalid Category provided",
                new Product()
        ));
      }
      var savedProduct = optionalProduct.get();
      BeanUtils.copyProperties(incomingProduct, savedProduct);
      savedProduct.setCategory(category.get());

      savedProduct = repository.save(savedProduct);
      return ResponseEntity.ok().body(new BaseResponse(
              0,
              "Success",
              savedProduct
      ));

    } catch (Exception e) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR)
              .body(new BaseResponse(
                      -999,
                      "Something wrong happened - " + e.getMessage(),
                      new Product()
              ));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteProduct(@RequestHeader HttpHeaders headers,
                                              @PathVariable Long id) {
    var validateResponse = sessionValidator.validateSession(headers);
    if (!validateResponse.isSuccess()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(validateResponse);
    }

    try {
      var optionalProduct = repository.findById(id);
      if (optionalProduct.isEmpty()) {
        return ResponseEntity.status(NOT_FOUND)
                .body(new BaseResponse(
                        -2,
                        "Nothing found with provided ID",
                        new Product()
                ));
      }
      repository.deleteById(id);
      return ResponseEntity.ok().body("Successful deleted!");

    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }
}