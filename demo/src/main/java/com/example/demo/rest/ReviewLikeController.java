package com.example.demo.rest;

import com.example.demo.dto.LikeDto;
import com.example.demo.dto.LikeStatsDto;
import com.example.demo.service.ReviewLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews/{reviewId}/likes")
@RequiredArgsConstructor
public class ReviewLikeController {

    private final ReviewLikeService reviewLikeService;

    @PostMapping
    public ResponseEntity<LikeDto> likeReview(@PathVariable Long reviewId) {
        try {
            LikeDto like = reviewLikeService.likeReview(reviewId);
            return ResponseEntity.status(HttpStatus.CREATED).body(like);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already liked")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> unlikeReview(@PathVariable Long reviewId) {
        try {
            reviewLikeService.unlikeReview(reviewId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<LikeStatsDto> getLikeStats(@PathVariable Long reviewId) {
        try {
            LikeStatsDto stats = reviewLikeService.getReviewLikeStats(reviewId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<LikeDto>> getReviewLikes(
            @PathVariable Long reviewId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<LikeDto> likes = reviewLikeService.getReviewLikes(reviewId, page, size);
            return ResponseEntity.ok(likes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Boolean>> hasUserLiked(@PathVariable Long reviewId) {
        try {
            LikeStatsDto stats = reviewLikeService.getReviewLikeStats(reviewId);
            boolean liked = Boolean.TRUE.equals(stats.getLikedByCurrentUser());
            Map<String, Boolean> response = new HashMap<>();
            response.put("liked", liked);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
