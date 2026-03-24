package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UserRepository extends JpaRepository <User,Long> {
    List<User> findByUserNameContainingIgnoreCase(String userName);
    Optional<User> findByEmailId(String emailId);
    Optional<User> findByUserName(String userName);

}
