package com.example.demo.repository;

import com.example.demo.entity.BookLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookLikeRepository extends JpaRepository<BookLike, Long> {
    Page<BookLike> findByBookId(Long bookId, Pageable pageable);

    Optional<BookLike> findByUserIdAndBookId(Long userId, Long bookId);

    Long countByBookId(Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
