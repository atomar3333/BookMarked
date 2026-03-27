package com.example.demo.rest;

import com.example.demo.dto.response.LikeResponseDto;
import com.example.demo.dto.response.LikeStatsResponseDto;
import com.example.demo.dto.response.LikedStateResponseDto;
import com.example.demo.service.BookLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books/{bookId}/likes")
@RequiredArgsConstructor
public class BookLikeController {

    private final BookLikeService bookLikeService;

    @PostMapping
    public ResponseEntity<LikeResponseDto> likeBook(@PathVariable Long bookId) {
        try {
            LikeResponseDto like = bookLikeService.likeBook(bookId);
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
    public ResponseEntity<LikeStatsResponseDto> getLikeStats(@PathVariable Long bookId) {
        try {
            LikeStatsResponseDto stats = bookLikeService.getBookLikeStats(bookId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<LikeResponseDto>> getBookLikes(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<LikeResponseDto> likes = bookLikeService.getBookLikes(bookId, page, size);
            return ResponseEntity.ok(likes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<LikedStateResponseDto> hasUserLiked(@PathVariable Long bookId) {
        try {
            LikeStatsResponseDto stats = bookLikeService.getBookLikeStats(bookId);
            boolean liked = Boolean.TRUE.equals(stats.getLikedByCurrentUser());
            return ResponseEntity.ok(new LikedStateResponseDto(liked));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
