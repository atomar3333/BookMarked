package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LikeDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long targetId;
    private LocalDateTime createdAt;
}
