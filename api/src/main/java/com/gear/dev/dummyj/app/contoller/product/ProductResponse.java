package com.gear.dev.dummyj.app.contoller.product;

import com.gear.dev.dummyj.core.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

  private List<Product> products;
  private int total;
  private int skip;
  private int limit;
}
