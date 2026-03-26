package com.example.demo.dto.request;

import com.example.demo.entity.ReadingStatusEnum;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateReadingStatusRequestDto {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "bookId is required")
    private Long bookId;

    @NotNull(message = "currentStatus is required")
    private ReadingStatusEnum currentStatus;

    private LocalDate startedAt;
    private LocalDate finishedAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public ReadingStatusEnum getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ReadingStatusEnum currentStatus) {
        this.currentStatus = currentStatus;
    }

    public LocalDate getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDate startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDate getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDate finishedAt) {
        this.finishedAt = finishedAt;
    }
}
