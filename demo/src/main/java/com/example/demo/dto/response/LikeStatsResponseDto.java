package com.example.demo.dto.response;

public class LikeStatsResponseDto {

    private Long likeCount;
    private Boolean likedByCurrentUser;

    public LikeStatsResponseDto() {
    }

    public LikeStatsResponseDto(Long likeCount, Boolean likedByCurrentUser) {
        this.likeCount = likeCount;
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(Boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }
}
