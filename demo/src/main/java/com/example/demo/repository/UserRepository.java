package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface UserRepository extends JpaRepository <User,Long> {
    List<User> findByUserNameContainingIgnoreCase(String userName);
    Optional<User> findByEmailId(String emailId);
    Optional<User> findByUserName(String userName);

    @Query("SELECT u FROM User u WHERE u.isProfilePublic = true OR u.id = :viewerId")
    Page<User> findVisibleToViewer(@Param("viewerId") Long viewerId, Pageable pageable);
}
