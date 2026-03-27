package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReviewResponseDto {

    private Long id;
    private Long userId;
    private Long bookId;
    private String reviewText;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LikeStatsResponseDto likeStats;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LikeStatsResponseDto getLikeStats() {
        return likeStats;
    }

    public void setLikeStats(LikeStatsResponseDto likeStats) {
        this.likeStats = likeStats;
    }
}
