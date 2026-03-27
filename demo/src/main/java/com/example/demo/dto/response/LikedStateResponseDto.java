package com.example.demo.dto.response;

public class LikedStateResponseDto {

    private boolean liked;

    public LikedStateResponseDto() {
    }

    public LikedStateResponseDto(boolean liked) {
        this.liked = liked;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
