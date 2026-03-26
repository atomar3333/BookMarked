package com.example.demo.dto.response;

import com.example.demo.entity.ReadingStatusEnum;

import java.time.LocalDate;

public class ReadingStatusResponseDto {

    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDate startedAt;
    private LocalDate finishedAt;
    private ReadingStatusEnum currentStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public ReadingStatusEnum getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ReadingStatusEnum currentStatus) {
        this.currentStatus = currentStatus;
    }
}
