package com.example.demo.dto;

import lombok.Data;

@Data
public class LikeStatsDto {
    private Long likeCount;
    private Boolean likedByCurrentUser;

    public LikeStatsDto() {
    }

    public LikeStatsDto(Long likeCount, Boolean likedByCurrentUser) {
        this.likeCount = likeCount;
        this.likedByCurrentUser = likedByCurrentUser;
    }
}
