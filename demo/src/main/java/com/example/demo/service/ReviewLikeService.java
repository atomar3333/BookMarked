package com.example.demo.service;

import com.example.demo.dto.LikeDto;
import com.example.demo.dto.LikeStatsDto;
import com.example.demo.entity.ActivityType;
import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewLike;
import com.example.demo.entity.User;
import com.example.demo.repository.ReviewLikeRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ActivityService activityService;

    @Transactional
    public LikeDto likeReview(Long reviewId) {
        Long currentUserId = getCurrentUserIdOrThrow();
        
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + currentUserId));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        if (reviewLikeRepository.existsByUserIdAndReviewId(currentUserId, reviewId)) {
            throw new RuntimeException("User has already liked this review");
        }

        ReviewLike like = new ReviewLike();
        like.setUser(user);
        like.setReview(review);

        try {
            ReviewLike saved = reviewLikeRepository.save(like);
            activityService.record(user, ActivityType.REVIEW_LIKED, review.getId(), Map.of(
                    "bookId", review.getBook().getId(),
                    "bookTitle", review.getBook().getTitle()
            ));
            return mapToDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("User has already liked this review");
        }
    }

    @Transactional
    public void unlikeReview(Long reviewId) {
        Long currentUserId = getCurrentUserIdOrThrow();
        
        ReviewLike like = reviewLikeRepository.findByUserIdAndReviewId(currentUserId, reviewId)
                .orElseThrow(() -> new RuntimeException("Like not found"));
        reviewLikeRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public Page<LikeDto> getReviewLikes(Long reviewId, int page, int size) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found with ID: " + reviewId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return reviewLikeRepository.findByReviewId(reviewId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public LikeStatsDto getReviewLikeStats(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found with ID: " + reviewId);
        }
        
        Long likeCount = reviewLikeRepository.countByReviewId(reviewId);
        
        Long currentUserId = getCurrentUserIdAttempt();
        Boolean likedByCurrentUser = false;
        if (currentUserId != null) {
            likedByCurrentUser = reviewLikeRepository.existsByUserIdAndReviewId(currentUserId, reviewId);
        }
        
        return new LikeStatsDto(likeCount, likedByCurrentUser);
    }

    @Transactional(readOnly = true)
    public boolean hasUserLikedReview(Long userId, Long reviewId) {
        return reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId);
    }

    private LikeDto mapToDto(ReviewLike like) {
        LikeDto dto = new LikeDto();
        dto.setId(like.getId());
        dto.setUserId(like.getUser().getId());
        dto.setUserName(like.getUser().getUserName());
        dto.setTargetId(like.getReview().getId());
        dto.setCreatedAt(like.getCreatedAt());
        return dto;
    }

    private Long getCurrentUserIdOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("User not authenticated");
        }
        String principal = authentication.getName();
        User user = userRepository.findByEmailId(principal)
            .or(() -> userRepository.findByUserName(principal))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    private Long getCurrentUserIdAttempt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        String principal = authentication.getName();
        return userRepository.findByEmailId(principal)
            .or(() -> userRepository.findByUserName(principal))
            .map(User::getId)
            .orElse(null);
    }
}
