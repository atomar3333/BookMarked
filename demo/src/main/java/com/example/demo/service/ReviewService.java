package com.example.demo.service;

import com.example.demo.dto.ReviewDto;
import com.example.demo.entity.Book;
import com.example.demo.entity.Role;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final BigDecimal MIN_RATING = new BigDecimal("1.0");
    private static final BigDecimal MAX_RATING = new BigDecimal("5.0");

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReviewDto createReview(ReviewDto request) {
        assertSelfOrAdmin(request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + request.getBookId()));

        if (request.getRating() == null
            || request.getRating().compareTo(MIN_RATING) < 0
            || request.getRating().compareTo(MAX_RATING) > 0) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setReviewText(request.getReviewText());
        review.setRating(request.getRating());
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        return mapToDto(saved);
    }

    public ReviewDto getReviewById(Long reviewId) {
        return mapToDto(reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId)));
    }

    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsByBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Book not found with ID: " + bookId);
        }
        return reviewRepository.findByBookId(bookId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return reviewRepository.findByUserId(userId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public ReviewDto updateReview(Long reviewId, ReviewDto request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        assertReviewOwnerOrAdmin(review);

        if (request.getReviewText() != null) {
            review.setReviewText(request.getReviewText());
        }
        if (request.getRating() != null) {
            if (request.getRating().compareTo(MIN_RATING) < 0
                    || request.getRating().compareTo(MAX_RATING) > 0) {
                throw new RuntimeException("Rating must be between 1 and 5");
            }
            review.setRating(request.getRating());
        }

        Review updated = reviewRepository.save(review);
        return mapToDto(updated);
    }

    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        assertReviewOwnerOrAdmin(review);
        reviewRepository.deleteById(reviewId);
    }

    public Double getAverageRatingByBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Book not found with ID: " + bookId);
        }
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        if (reviews.isEmpty()) return 0.0;
        double sum = reviews.stream().mapToDouble(review -> review.getRating().doubleValue()).sum();
        return sum / reviews.size();
    }

    private ReviewDto mapToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        dto.setBookId(review.getBook().getId());
        dto.setReviewText(review.getReviewText());
        dto.setRating(review.getRating());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    private User getCurrentUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        return userRepository.findByEmailId(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    private void assertSelfOrAdmin(Long targetUserId) {
        User currentUser = getCurrentUserOrThrow();
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You can only create reviews for your own account");
        }
    }

    private void assertReviewOwnerOrAdmin(Review review) {
        User currentUser = getCurrentUserOrThrow();
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !currentUser.getId().equals(review.getUser().getId())) {
            throw new AccessDeniedException("You can only modify your own reviews");
        }
    }
}
