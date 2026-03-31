package com.example.demo.dto.response;

import com.example.demo.entity.ReadingStatusEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReadProgressResponseDto {
    private Long id;
    private Long userId;
    private Long bookId;
    private Integer currentPosition;
    private Integer totalPages;
    private ReadingStatusEnum status;
    private LocalDate startedAt;
    private LocalDate finishedAt;
    private Integer readNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
