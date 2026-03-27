package com.example.demo.rest;

import com.example.demo.dto.request.CreateGenreRequestDto;
import com.example.demo.dto.request.UpdateGenreRequestDto;
import com.example.demo.dto.response.GenreResponseDto;
import jakarta.validation.Valid;
import com.example.demo.entity.Book;
import com.example.demo.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<GenreResponseDto> createGenre(@Valid @RequestBody CreateGenreRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genreService.createGenre(request));
    }

    @GetMapping("/{genreId}")
    public ResponseEntity<GenreResponseDto> getGenreById(@PathVariable Long genreId) {
        try {
            return ResponseEntity.ok(genreService.getGenreById(genreId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<GenreResponseDto>> getAllGenres(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(genreService.getAllGenres(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<List<GenreResponseDto>> searchGenres(@RequestParam String name) {
        return ResponseEntity.ok(genreService.searchGenres(name));
    }

    @PutMapping("/{genreId}")
    public ResponseEntity<GenreResponseDto> updateGenre(@PathVariable Long genreId, @Valid @RequestBody UpdateGenreRequestDto request) {
        try {
            return ResponseEntity.ok(genreService.updateGenre(genreId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{genreId}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long genreId) {
        try {
            genreService.deleteGenre(genreId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{genreId}/books/{bookId}")
    public ResponseEntity<Void> linkGenreToBook(@PathVariable Long genreId, @PathVariable Long bookId) {
        try {
            genreService.linkGenreToBook(genreId, bookId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{genreId}/books/{bookId}")
    public ResponseEntity<Void> unlinkGenreFromBook(@PathVariable Long genreId, @PathVariable Long bookId) {
        try {
            genreService.unlinkGenreFromBook(genreId, bookId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{genreId}/books")
    public ResponseEntity<List<Book>> getBooksByGenre(@PathVariable Long genreId) {
        try {
            return ResponseEntity.ok(genreService.getBooksByGenre(genreId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<List<GenreResponseDto>> getGenresByBook(@PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(genreService.getGenresByBook(bookId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
