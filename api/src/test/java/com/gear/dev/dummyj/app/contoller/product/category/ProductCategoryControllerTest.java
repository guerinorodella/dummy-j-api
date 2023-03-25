package com.gear.dev.dummyj.app.contoller.product.category;

import com.gear.dev.dummyj.app.contoller.BaseResponse;
import com.gear.dev.dummyj.app.contoller.product.ProductResponse;
import com.gear.dev.dummyj.app.validations.SessionValidator;
import com.gear.dev.dummyj.core.model.Product;
import com.gear.dev.dummyj.core.model.ProductCategory;
import com.gear.dev.dummyj.core.repository.ProductCategoryRepository;
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
class ProductCategoryControllerTest {
  @Mock
  private ProductCategoryRepository repository;
  @Mock
  private AuthorizationService authService;
  private SessionValidator sessionValidator;
  private ProductCategoryController instance;
  private HttpHeaders defaultValidHeader;

  @BeforeEach
  void setUp() {
    defaultValidHeader = new HttpHeaders();
    defaultValidHeader.set(HttpHeaders.AUTHORIZATION, "Bearer 123456");
    sessionValidator = new SessionValidator(authService);
    instance = new ProductCategoryController(repository, sessionValidator);
  }

  @Test
  void findAllProductCategory_returnsSuccessResponse_forValidToken() {
    var stringCaptor = ArgumentCaptor.forClass(String.class);
    when(authService.isUserAuthenticated(stringCaptor.capture())).thenReturn(true);
    List<ProductCategory> allCategoryList = Arrays.asList(
            new ProductCategory(1L, "Coffee"),
            new ProductCategory(2L, "Tea"));
    when(repository.findAll()).thenReturn(allCategoryList);
    var expectedResponseBody = new BaseResponse(0, "Success", allCategoryList);

    var response = instance.getAllProductCategory(defaultValidHeader);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getData());
    assertEquals(OK, response.getStatusCode());
    assertTrue(new ReflectionEquals(expectedResponseBody).matches(response.getBody()));
    assertEquals("123456", stringCaptor.getValue());
  }
}