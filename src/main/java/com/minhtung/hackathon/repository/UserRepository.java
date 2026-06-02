package com.minhtung.hackathon.repository;

import com.minhtung.hackathon.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    Optional<User>findByToken(String token);

    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username); ;


    boolean existsByUsername(String username);
    List<User> findByActiveFalseAndExpiredAtBefore(LocalDateTime time);

}

