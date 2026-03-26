package com.example.demo.dto.request;

import com.example.demo.entity.ReadingStatusEnum;

import java.time.LocalDate;

public class UpdateReadingStatusRequestDto {

    private ReadingStatusEnum currentStatus;
    private LocalDate startedAt;
    private LocalDate finishedAt;

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
