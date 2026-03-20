package com.example.demo.dto;

import com.example.demo.entity.ReadingStatusEnum;

import java.time.LocalDate;

public class ReadingStatusDto {
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDate startedAt;
    private LocalDate finishedAt;
    private ReadingStatusEnum currentStatus;

    public ReadingStatusDto() {
    }

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

    public ReadingStatusEnum getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ReadingStatusEnum currentStatus) {
        this.currentStatus = currentStatus;
    }



    public void setFinishedAt(LocalDate finishDate) {
        if (finishDate != null && this.startedAt != null && finishDate.isBefore(this.startedAt)) {
            throw new IllegalArgumentException("Finish date cannot be before start date");
        }

        // Only allow finishedAt to be set if status is COMPLETED or ON_HOLD
        if (finishDate != null && this.currentStatus != ReadingStatusEnum.CURRENTLY_READING ) {
            throw new IllegalArgumentException(
                    "finishedAt can only be set for COMPLETED");
        }

        this.finishedAt = finishDate;
    }

//    public ReadingStatusEnum getReadingStatus() {
//        return currentStatus;
//    }
//
//    public void setReadingStatus(ReadingStatusEnum readingStatus) {
//        this.currentStatus = readingStatus;
//    }
}
