package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateReadingSessionRequestDto {
    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "readProgressId is required")
    private Long readProgressId;

    private Integer durationMinutes;
    private Integer startPosition;
    private Integer endPosition;
    private LocalDate sessionDate;
    private String notes;
}
