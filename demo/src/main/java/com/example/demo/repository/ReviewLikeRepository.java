package com.example.demo.repository;

import com.example.demo.entity.ReviewLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Page<ReviewLike> findByReviewId(Long reviewId, Pageable pageable);

    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);

    Long countByReviewId(Long reviewId);

    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);
}
