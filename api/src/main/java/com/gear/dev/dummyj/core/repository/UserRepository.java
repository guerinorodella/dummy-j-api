package com.gear.dev.dummyj.core.repository;

import com.gear.dev.dummyj.core.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
  UserModel findByUserNameAndPassword(String userName, String password);
}
