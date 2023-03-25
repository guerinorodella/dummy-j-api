package com.gear.dev.dummyj.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_name", nullable = false)
  private String userName;

  @Column
  private String password;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "created_time", nullable = false)
  private LocalDateTime createdTime;

  @Column
  private int status;
}
