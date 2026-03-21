package com.example.demo.rest;

import com.example.demo.dto.ReadingStatusDto;
import com.example.demo.service.ReadingStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-status")
@RequiredArgsConstructor
public class ReadingStatusController {
    private final ReadingStatusService readingStatusService;

    @PostMapping
    public ResponseEntity<ReadingStatusDto> createReadingStatus(@RequestBody ReadingStatusDto payload) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(readingStatusService.createReadingStatus(payload));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{statusId}")
    public ResponseEntity<ReadingStatusDto> getReadingStatusById(@PathVariable Long statusId) {
        try {
            return ResponseEntity.ok(readingStatusService.getReadingStatusById(statusId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ReadingStatusDto>> getAllReadingStatuses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(readingStatusService.getAllReadingStatuses(page, size));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReadingStatusDto>> getReadingStatusesByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(readingStatusService.getReadingStatusesByUser(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<List<ReadingStatusDto>> getReadingStatusesByBook(@PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(readingStatusService.getReadingStatusesByBook(bookId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/users/{userId}/books/{bookId}")
    public ResponseEntity<ReadingStatusDto> getReadingStatusForUserBook(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(readingStatusService.getReadingStatusForUserBook(userId, bookId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{statusId}")
    public ResponseEntity<ReadingStatusDto> updateReadingStatus(
            @PathVariable Long statusId,
            @RequestBody ReadingStatusDto payload) {
        try {
            return ResponseEntity.ok(readingStatusService.updateReadingStatus(statusId, payload));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{statusId}")
    public ResponseEntity<Void> deleteReadingStatus(@PathVariable Long statusId) {
        try {
            readingStatusService.deleteReadingStatus(statusId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
