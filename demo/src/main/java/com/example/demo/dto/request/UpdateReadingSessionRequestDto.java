package com.example.demo.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateReadingSessionRequestDto {
    private Integer durationMinutes;
    private Integer startPosition;
    private Integer endPosition;
    private LocalDate sessionDate;
    private String notes;
}
