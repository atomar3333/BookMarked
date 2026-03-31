package com.example.demo.rest;

import com.example.demo.dto.request.CreateReadProgressRequestDto;
import com.example.demo.dto.request.UpdateReadProgressRequestDto;
import com.example.demo.dto.response.ReadProgressResponseDto;
import com.example.demo.service.ReadProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/read-progress")
@RequiredArgsConstructor
public class ReadProgressController {

    private final ReadProgressService readProgressService;

    @PostMapping
    public ResponseEntity<ReadProgressResponseDto> createReadProgress(
            @Valid @RequestBody CreateReadProgressRequestDto payload) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(readProgressService.createReadProgress(payload));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadProgressResponseDto> getReadProgressById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(readProgressService.getReadProgressById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReadProgressResponseDto>> getReadProgressesByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(readProgressService.getReadProgressesByUser(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<List<ReadProgressResponseDto>> getReadProgressesByBook(@PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(readProgressService.getReadProgressesByBook(bookId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/users/{userId}/books/{bookId}")
    public ResponseEntity<List<ReadProgressResponseDto>> getReadProgressesForUserBook(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(readProgressService.getReadProgressesForUserBook(userId, bookId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReadProgressResponseDto> updateReadProgress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReadProgressRequestDto payload) {
        try {
            return ResponseEntity.ok(readProgressService.updateReadProgress(id, payload));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReadProgress(@PathVariable Long id) {
        try {
            readProgressService.deleteReadProgress(id);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
