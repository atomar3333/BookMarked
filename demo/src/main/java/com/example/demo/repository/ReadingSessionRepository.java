package com.example.demo.repository;

import com.example.demo.entity.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    List<ReadingSession> findByReadProgressId(Long readProgressId);
    List<ReadingSession> findByUserId(Long userId);
}
