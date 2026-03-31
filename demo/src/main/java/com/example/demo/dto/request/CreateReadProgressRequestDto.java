package com.example.demo.dto.request;

import com.example.demo.entity.ReadingStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateReadProgressRequestDto {
    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "bookId is required")
    private Long bookId;

    private Integer currentPosition;
    private Integer totalPages;
    private ReadingStatusEnum status;
    private LocalDate startedAt;
    private LocalDate finishedAt;
    private Integer readNumber;
}
