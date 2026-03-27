package com.example.demo.rest;

import com.example.demo.dto.response.LikeResponseDto;
import com.example.demo.dto.response.LikeStatsResponseDto;
import com.example.demo.dto.response.LikedStateResponseDto;
import com.example.demo.service.ListLikeService;
import com.example.demo.service.ListsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lists/{listId}/likes")
@RequiredArgsConstructor
public class ListLikeController {

    private final ListLikeService listLikeService;
    private final ListsService listsService;

    @PostMapping
    public ResponseEntity<LikeResponseDto> likeList(@PathVariable Long listId) {
        try {
            LikeResponseDto like = listLikeService.likeList(listId);
            return ResponseEntity.status(HttpStatus.CREATED).body(like);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already liked")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> unlikeList(@PathVariable Long listId) {
        try {
            listLikeService.unlikeList(listId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<LikeStatsResponseDto> getLikeStats(@PathVariable Long listId) {
        try {
            listsService.assertListVisibility(listId);
            LikeStatsResponseDto stats = listLikeService.getListLikeStats(listId);
            return ResponseEntity.ok(stats);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<LikeResponseDto>> getListLikes(
            @PathVariable Long listId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<LikeResponseDto> likes = listLikeService.getListLikes(listId, page, size);
            return ResponseEntity.ok(likes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<LikedStateResponseDto> hasUserLiked(@PathVariable Long listId) {
        try {
            LikeStatsResponseDto stats = listLikeService.getListLikeStats(listId);
            boolean liked = Boolean.TRUE.equals(stats.getLikedByCurrentUser());
            return ResponseEntity.ok(new LikedStateResponseDto(liked));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
