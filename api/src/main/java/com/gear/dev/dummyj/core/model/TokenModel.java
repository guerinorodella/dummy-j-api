package com.gear.dev.dummyj.core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "authorization_tokens")
@NoArgsConstructor
@AllArgsConstructor
public class TokenModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id")
  private UserModel user;
  @Column
  private String token;

  @Column(name = "created_time", nullable = false)
  private Date createdTime;

  @Column(name = "expiry_time", nullable = false)
  private Date expiryTime;
}
