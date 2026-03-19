package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UserRepository extends JpaRepository <User,Long> {
//    List<User> findAll(String userName);
//    Optional<User> findByUserName(String userName);
//    Optional<User> findByEmail(String emailId);


}
