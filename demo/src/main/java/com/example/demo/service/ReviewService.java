package com.example.demo.service;

import com.example.demo.dto.ReviewDto;
import com.example.demo.entity.Book;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReviewDto createReview(ReviewDto request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + request.getBookId()));

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
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

        if (request.getReviewText() != null) {
            review.setReviewText(request.getReviewText());
        }
        if (request.getRating() != null) {
            if (request.getRating() < 1 || request.getRating() > 5) {
                throw new RuntimeException("Rating must be between 1 and 5");
            }
            review.setRating(request.getRating());
        }

        Review updated = reviewRepository.save(review);
        return mapToDto(updated);
    }

    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found with ID: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    public Double getAverageRatingByBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Book not found with ID: " + bookId);
        }
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        if (reviews.isEmpty()) return 0.0;
        double sum = reviews.stream().mapToDouble(Review::getRating).sum();
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
}
