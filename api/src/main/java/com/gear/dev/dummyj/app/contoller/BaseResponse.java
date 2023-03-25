package com.gear.dev.dummyj.app.contoller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gear.dev.dummyj.core.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BaseResponse<T> {

  private static final int SUCCESS_CODE = 0;
  private static final String SUCCESS_MESSAGE = "Success";
  private int errorCode;

  private String message;

  private T data;

  public static <E> BaseResponse<E> success(E value) {
    return new BaseResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, value);
  }

  @JsonIgnore
  public boolean isSuccess() {
    return SUCCESS_CODE == errorCode;
  }
}
