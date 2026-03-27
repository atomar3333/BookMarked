package com.example.demo.rest;

import com.example.demo.dto.request.CreateAuthorRequestDto;
import com.example.demo.dto.request.UpdateAuthorRequestDto;
import com.example.demo.dto.response.AuthorResponseDto;
import jakarta.validation.Valid;
import com.example.demo.entity.Book;
import com.example.demo.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    public ResponseEntity<AuthorResponseDto> createAuthor(@Valid @RequestBody CreateAuthorRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authorService.createAuthor(request));
    }

    @GetMapping("/{authorId}")
    public ResponseEntity<AuthorResponseDto> getAuthorById(@PathVariable Long authorId) {
        try {
            return ResponseEntity.ok(authorService.getAuthorById(authorId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<AuthorResponseDto>> getAllAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(authorService.getAllAuthors(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AuthorResponseDto>> searchAuthors(@RequestParam String name) {
        return ResponseEntity.ok(authorService.searchAuthors(name));
    }

    @PutMapping("/{authorId}")
    public ResponseEntity<AuthorResponseDto> updateAuthor(@PathVariable Long authorId, @Valid @RequestBody UpdateAuthorRequestDto request) {
        try {
            return ResponseEntity.ok(authorService.updateAuthor(authorId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{authorId}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long authorId) {
        try {
            authorService.deleteAuthor(authorId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Link / unlink author ↔ book
    @PostMapping("/{authorId}/books/{bookId}")
    public ResponseEntity<Void> linkAuthorToBook(@PathVariable Long authorId, @PathVariable Long bookId) {
        try {
            authorService.linkAuthorToBook(authorId, bookId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{authorId}/books/{bookId}")
    public ResponseEntity<Void> unlinkAuthorFromBook(@PathVariable Long authorId, @PathVariable Long bookId) {
        try {
            authorService.unlinkAuthorFromBook(authorId, bookId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Get books by author
    @GetMapping("/{authorId}/books")
    public ResponseEntity<List<Book>> getBooksByAuthor(@PathVariable Long authorId) {
        try {
            return ResponseEntity.ok(authorService.getBooksByAuthor(authorId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Get authors of a book
    @GetMapping("/books/{bookId}")
    public ResponseEntity<List<AuthorResponseDto>> getAuthorsByBook(@PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(authorService.getAuthorsByBook(bookId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
