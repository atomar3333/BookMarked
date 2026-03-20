package com.example.demo.rest;

import com.example.demo.dto.FollowerDto;
import com.example.demo.service.FollowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/followers")
@RequiredArgsConstructor
public class FollowerController {

    private final FollowerService followerService;

    // POST /api/followers/{followerId}/follow/{followingId}
    @PostMapping("/{followerId}/follow/{followingId}")
    public ResponseEntity<FollowerDto> follow(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(followerService.follow(followerId, followingId));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Already following") || e.getMessage().contains("cannot follow themselves")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // DELETE /api/followers/{followerId}/unfollow/{followingId}
    @DeleteMapping("/{followerId}/unfollow/{followingId}")
    public ResponseEntity<Void> unfollow(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        try {
            followerService.unfollow(followerId, followingId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // GET /api/followers/{userId}/followers — people who follow userId
    @GetMapping("/{userId}/followers")
    public ResponseEntity<Page<FollowerDto>> getFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(followerService.getFollowers(userId, page, size));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // GET /api/followers/{userId}/following — people userId is following
    @GetMapping("/{userId}/following")
    public ResponseEntity<Page<FollowerDto>> getFollowing(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(followerService.getFollowing(userId, page, size));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // GET /api/followers/{userId}/followers/count
    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<Map<String, Object>> getFollowerCount(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(followerService.getFollowerCount(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // GET /api/followers/{userId}/following/count
    @GetMapping("/{userId}/following/count")
    public ResponseEntity<Map<String, Object>> getFollowingCount(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(followerService.getFollowingCount(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
