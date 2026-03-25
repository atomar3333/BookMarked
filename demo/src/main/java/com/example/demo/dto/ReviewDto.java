package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReviewDto {
    private Long id;
    private Long userId;
    private Long bookId;
    private String reviewText;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LikeStatsDto likeStats;

    public ReviewDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LikeStatsDto getLikeStats() { return likeStats; }
    public void setLikeStats(LikeStatsDto likeStats) { this.likeStats = likeStats; }
}
