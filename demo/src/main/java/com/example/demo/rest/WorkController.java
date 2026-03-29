package com.example.demo.rest;

import com.example.demo.dto.request.AddWorkRequestDto;
import com.example.demo.entity.Work;
import com.example.demo.service.WorkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @PostMapping("/add-work")
    public ResponseEntity<Work> createWork(@Valid @RequestBody AddWorkRequestDto request) {
        Work created = workService.createWork(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{workId}")
    public ResponseEntity<Work> getWorkById(@PathVariable Long workId) {
        try {
            return ResponseEntity.ok(workService.getWorkById(workId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<Work>> getAllWorks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(workService.getAllWorks(page, size));
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<Work>> searchByTitle(@RequestParam String title) {
        return ResponseEntity.ok(workService.searchWorksByTitle(title));
    }

    @GetMapping("/search/author")
    public ResponseEntity<List<Work>> searchByAuthor(@RequestParam String author) {
        return ResponseEntity.ok(workService.searchWorksByAuthor(author));
    }

    @PutMapping("/{workId}")
    public ResponseEntity<Work> updateWork(@PathVariable Long workId, @Valid @RequestBody AddWorkRequestDto request) {
        try {
            return ResponseEntity.ok(workService.updateWork(workId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{workId}")
    public ResponseEntity<Void> deleteWork(@PathVariable Long workId) {
        workService.deleteWork(workId);
        return ResponseEntity.noContent().build();
    }
}
