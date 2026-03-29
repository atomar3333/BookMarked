package com.example.demo.repository;

import com.example.demo.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long> {
    Optional<Work> findByOpenLibraryWorkId(String openLibraryWorkId);
    List<Work> findByTitleContainingIgnoreCase(String title);
    List<Work> findByAuthorsContainingIgnoreCase(String author);
}
