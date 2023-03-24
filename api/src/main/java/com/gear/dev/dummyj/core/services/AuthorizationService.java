package com.gear.dev.dummyj.core.services;

import com.gear.dev.dummyj.core.model.UserModel;

public interface AuthorizationService {

  UserModel findUser(String userName, String password);

  String generateAuthToken(UserModel user);

  void updateLastToken(UserModel user, String authToken);

  boolean isUserAuthenticated(String authToken);

  boolean isUserBlocked(UserModel user);

  String renewToken(String lastToken);
}
