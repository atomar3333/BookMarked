package com.example.demo.repository;

import com.example.demo.entity.ReadingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingStatusRepository extends JpaRepository<ReadingStatus,Long> {
    List<ReadingStatus> findByBookId(Long bookId);
    List<ReadingStatus> findByUserId(Long userId);
    Optional<ReadingStatus> findByUserIdAndBookId(Long userId, Long bookId);
}

