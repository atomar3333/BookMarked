package com.example.demo.dto.request;

import com.example.demo.entity.ReadingStatusEnum;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateReadProgressRequestDto {
    private Integer currentPosition;
    private Integer totalPages;
    private ReadingStatusEnum status;
    private LocalDate startedAt;
    private LocalDate finishedAt;
    private Integer readNumber;
}
