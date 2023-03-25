package com.gear.dev.dummyj.app.contoller.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequest {

  private String title;

  private String description;

  private double price;

  private double discountPercentage;

  private double rating;

  private int stock;

  private String brand;

  private Long category;

  private String thumbnail;

  private String images;// @TODO must be mapped to list
}
