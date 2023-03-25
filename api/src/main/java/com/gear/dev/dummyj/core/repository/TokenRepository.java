package com.gear.dev.dummyj.core.repository;

import com.gear.dev.dummyj.core.model.TokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<TokenModel, Long> {
  Optional<TokenModel> findByToken(String jwtToken);
}
