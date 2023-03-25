package com.gear.dev.dummyj.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.EAGER;

@Data
@Table(name = "product")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title")
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "price")
  private double price;

  @Column(name = "discount_percentage")
  private double discountPercentage;

  @Column(name = "rating")
  private double rating;

  @Column(name = "stock")
  private int stock;

  @Column(name = "brand")
  private String brand;

  @OneToOne(fetch = EAGER)
  @JoinColumn(name = "category_id")
  private ProductCategory category;

  @Column(name = "thumbnail")
  private String thumbnail;

  @Column(name = "images")
  private String images;// @TODO must be mapped to list
}
