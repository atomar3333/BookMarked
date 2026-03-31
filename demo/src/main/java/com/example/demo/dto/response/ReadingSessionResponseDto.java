package com.example.demo.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReadingSessionResponseDto {
    private Long id;
    private Long userId;
    private Long readProgressId;
    private Integer durationMinutes;
    private Integer startPosition;
    private Integer endPosition;
    private LocalDate sessionDate;
    private String notes;
    private LocalDateTime createdAt;
}
