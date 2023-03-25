package com.gear.dev.dummyj.app.contoller.product;

import com.gear.dev.dummyj.app.contoller.BaseResponse;
import com.gear.dev.dummyj.app.validations.SessionValidator;
import com.gear.dev.dummyj.core.model.Product;
import com.gear.dev.dummyj.core.model.ProductCategory;
import com.gear.dev.dummyj.core.repository.ProductCategoryRepository;
import com.gear.dev.dummyj.core.repository.ProductRepository;
import com.gear.dev.dummyj.core.services.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@DataJpaTest
@ActiveProfiles("test")
class ProductControllerTest {

  @Mock
  private ProductRepository repository;
  @Mock
  private ProductCategoryRepository categoryRepository;
  @Mock
  private AuthorizationService authService;
  private SessionValidator sessionValidator;
  private ProductController instance;
  private HttpHeaders defaultValidHeader;

  @BeforeEach
  void setUp() {
    defaultValidHeader = new HttpHeaders();
    defaultValidHeader.set(HttpHeaders.AUTHORIZATION, "Bearer 123456");
    sessionValidator = new SessionValidator(authService);
    instance = new ProductController(repository, categoryRepository, sessionValidator);
  }

  @Test
  void findAllProducts_returnsSuccessResponse_forValidToken() {
    var stringCaptor = ArgumentCaptor.forClass(String.class);
    when(authService.isUserAuthenticated(stringCaptor.capture())).thenReturn(true);
    List<Product> allProductList = Arrays.asList(
            new Product(43L, "Caramel Candy Coffee (Mocca)", "Mocca coffee Caramel Candy", 43.9, 0, 4.59, 100, "Mocca", new ProductCategory(1L, "Coffee"), "thumb", "image"),
            new Product(44L, "Coffee Pelé", "Café Pelé", 13.9, 0, 2.59, 10, "Pelé", new ProductCategory(1L, "Coffee"), "thumb", "image"));
    var expectedResponseBody = new BaseResponse(0, "Success",
            new ProductResponse(allProductList, allProductList.size(), 0, allProductList.size() / 2));
    when(repository.findAll()).thenReturn(allProductList);

    var response = instance.getAllProducts(defaultValidHeader);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getData());
    assertEquals(OK, response.getStatusCode());
    assertTrue(new ReflectionEquals(expectedResponseBody).matches(response.getBody()));
    assertEquals("123456", stringCaptor.getValue());
  }
}