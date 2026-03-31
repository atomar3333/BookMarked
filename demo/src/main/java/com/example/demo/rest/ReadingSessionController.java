package com.example.demo.rest;

import com.example.demo.dto.request.CreateReadingSessionRequestDto;
import com.example.demo.dto.request.UpdateReadingSessionRequestDto;
import com.example.demo.dto.response.ReadingSessionResponseDto;
import com.example.demo.service.ReadingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-sessions")
@RequiredArgsConstructor
public class ReadingSessionController {

    private final ReadingSessionService readingSessionService;

    @PostMapping
    public ResponseEntity<ReadingSessionResponseDto> createReadingSession(
            @Valid @RequestBody CreateReadingSessionRequestDto payload) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(readingSessionService.createReadingSession(payload));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadingSessionResponseDto> getReadingSessionById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(readingSessionService.getReadingSessionById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/read-progress/{readProgressId}")
    public ResponseEntity<List<ReadingSessionResponseDto>> getSessionsByReadProgress(
            @PathVariable Long readProgressId) {
        try {
            return ResponseEntity.ok(readingSessionService.getSessionsByReadProgress(readProgressId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReadingSessionResponseDto>> getSessionsByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(readingSessionService.getSessionsByUser(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReadingSessionResponseDto> updateReadingSession(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReadingSessionRequestDto payload) {
        try {
            return ResponseEntity.ok(readingSessionService.updateReadingSession(id, payload));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReadingSession(@PathVariable Long id) {
        try {
            readingSessionService.deleteReadingSession(id);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
