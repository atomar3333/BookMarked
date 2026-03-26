package com.example.demo.rest;

import com.example.demo.dto.LikeDto;
import com.example.demo.dto.LikeStatsDto;
import com.example.demo.service.BookLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/books/{bookId}/likes")
@RequiredArgsConstructor
public class BookLikeController {

    private final BookLikeService bookLikeService;

    @PostMapping
    public ResponseEntity<LikeDto> likeBook(@PathVariable Long bookId) {
        try {
            LikeDto like = bookLikeService.likeBook(bookId);
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
    public ResponseEntity<Void> unlikeBook(@PathVariable Long bookId) {
        try {
            bookLikeService.unlikeBook(bookId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<LikeStatsDto> getLikeStats(@PathVariable Long bookId) {
        try {
            LikeStatsDto stats = bookLikeService.getBookLikeStats(bookId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<LikeDto>> getBookLikes(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<LikeDto> likes = bookLikeService.getBookLikes(bookId, page, size);
            return ResponseEntity.ok(likes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Boolean>> hasUserLiked(@PathVariable Long bookId) {
        try {
            LikeStatsDto stats = bookLikeService.getBookLikeStats(bookId);
            boolean liked = Boolean.TRUE.equals(stats.getLikedByCurrentUser());
            Map<String, Boolean> response = new HashMap<>();
            response.put("liked", liked);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
