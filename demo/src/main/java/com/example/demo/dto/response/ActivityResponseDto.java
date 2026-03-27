package com.example.demo.dto.response;

import com.example.demo.entity.ActivityType;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class ActivityResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private ActivityType activityType;
    private Long targetId;
    private JsonNode metadata;
    private LocalDateTime createdAt;

    public ActivityResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public JsonNode getMetadata() { return metadata; }
    public void setMetadata(JsonNode metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
