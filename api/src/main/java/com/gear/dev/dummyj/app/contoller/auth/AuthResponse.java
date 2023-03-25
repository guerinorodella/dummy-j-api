package com.gear.dev.dummyj.app.contoller.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

  private String token;
  // BLOCKED
  // USER NOT FOUND
  private String status;
}
