package com.example.demo.rest;

import com.example.demo.dto.ActivityDto;
import com.example.demo.entity.ActivityType;
import com.example.demo.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/me")
    public ResponseEntity<Page<ActivityDto>> getMyActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ActivityType type) {
        return ResponseEntity.ok(activityService.getMyActivities(page, size, type));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<ActivityDto>> getUserActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ActivityType type) {
        try {
            return ResponseEntity.ok(activityService.getUserActivities(userId, page, size, type));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<ActivityDto>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ActivityType type) {
        return ResponseEntity.ok(activityService.getFeed(page, size, type));
    }
}
