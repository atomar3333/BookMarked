package com.example.demo.rest;

import com.example.demo.dto.ReviewDto;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@RequestBody ReviewDto payload) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(payload));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable Long reviewId) {
        try {
            return ResponseEntity.ok(reviewService.getReviewById(reviewId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByBook(@PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(reviewService.getReviewsByBook(bookId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long reviewId, @RequestBody ReviewDto payload) {
        try {
            return ResponseEntity.ok(reviewService.updateReview(reviewId, payload));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/books/{bookId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(reviewService.getAverageRatingByBook(bookId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
