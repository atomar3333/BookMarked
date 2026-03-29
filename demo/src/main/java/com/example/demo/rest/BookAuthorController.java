package com.example.demo.rest;

import com.example.demo.dto.response.BookAuthorResponseDto;
import com.example.demo.service.BookAuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-authors")
public class BookAuthorController {

    // Best Practice: Constructor injection is safer than @Autowired field injection
    private final BookAuthorService bookAuthorService;

    public BookAuthorController(BookAuthorService bookAuthorService) {
        this.bookAuthorService = bookAuthorService;
    }

    @PostMapping("/book/{bookId}/author/{authorId}")
    public ResponseEntity<?> addBookAuthor(@PathVariable Long bookId, @PathVariable Long authorId) {
        try {
            // Our updated service now returns a DTO instead of a boolean
            BookAuthorResponseDto response = bookAuthorService.addBookAuthor(bookId, authorId);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Catches the "id not in database" exception we threw in the service
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            // Catch-all for database connection drops or unexpected server errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while adding the relationship.");
        }
    }

    @DeleteMapping("/book/{bookId}/author/{authorId}")
    public ResponseEntity<?> removeBookAuthor(@PathVariable Long bookId, @PathVariable Long authorId) {
        try {
            boolean success = bookAuthorService.removeBookAuthor(bookId, authorId);
            if (!success) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Relationship between book and author not found.");
            }
            return ResponseEntity.ok().body("Relationship removed successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while removing the relationship.");
        }
    }

    @GetMapping("/book/{bookId}/authors")
    public ResponseEntity<?> getAuthorIdsForBook(@PathVariable Long bookId) {
        try {
            List<Long> authorIds = bookAuthorService.getAuthorIdsForBook(bookId);
            return ResponseEntity.ok(authorIds);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve authors for the specified book.");
        }
    }

    @GetMapping("/author/{authorId}/books")
    public ResponseEntity<?> getBookIdsForAuthor(@PathVariable Long authorId) {
        try {
            List<Long> bookIds = bookAuthorService.getBookIdsForAuthor(authorId);
            return ResponseEntity.ok(bookIds);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve books for the specified author.");
        }
    }
}