package com.example.demo.repository;

import com.example.demo.entity.ReadProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadProgressRepository extends JpaRepository<ReadProgress, Long> {
    List<ReadProgress> findByBookId(Long bookId);
    List<ReadProgress> findByUserId(Long userId);
    List<ReadProgress> findByUserIdAndBookId(Long userId, Long bookId);
    Optional<ReadProgress> findTopByUserIdAndBookIdOrderByCreatedAtDesc(Long userId, Long bookId);
}
