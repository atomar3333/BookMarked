package com.example.demo.rest;

import com.example.demo.dto.BookListDto;
import com.example.demo.dto.CreateListDto;
import com.example.demo.dto.ListDto;
import com.example.demo.dto.UpdateListDto;
import com.example.demo.service.BookListService;
import com.example.demo.service.ListsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class ListsController {

    private final ListsService listsService;
    private final BookListService bookListService;

    @PostMapping
    public ResponseEntity<ListDto> createList(@RequestBody CreateListDto payload) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(listsService.createList(payload));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{listId}")
    public ResponseEntity<ListDto> getListById(@PathVariable Long listId) {
        try {
            return ResponseEntity.ok(listsService.getListById(listId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ListDto>> getAllLists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(listsService.getAllLists(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ListDto>> searchLists(@RequestParam String query) {
        try {
            return ResponseEntity.ok(listsService.searchLists(query));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<ListDto>> getListsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(listsService.getListsByUser(userId, page, size));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{listId}")
    public ResponseEntity<ListDto> updateList(
            @PathVariable Long listId,
            @RequestBody UpdateListDto payload) {
        try {
            return ResponseEntity.ok(listsService.updateList(listId, payload));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(@PathVariable Long listId) {
        try {
            listsService.deleteList(listId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{listId}/books/{bookId}")
    public ResponseEntity<BookListDto> addBookToList(
            @PathVariable Long listId,
            @PathVariable Long bookId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(bookListService.addBookToList(listId, bookId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already in this list")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{listId}/books/{bookId}")
    public ResponseEntity<Void> removeBookFromList(
            @PathVariable Long listId,
            @PathVariable Long bookId) {
        try {
            bookListService.removeBookFromList(listId, bookId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{listId}/books")
    public ResponseEntity<Page<BookListDto>> getBooksInList(
            @PathVariable Long listId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(bookListService.getBooksInList(listId, page, size));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{listId}/books/count")
    public ResponseEntity<Map<String, Object>> getBookCountInList(@PathVariable Long listId) {
        try {
            long count = bookListService.getBookCountInList(listId);
            Map<String, Object> response = new HashMap<>();
            response.put("listId", listId);
            response.put("bookCount", count);
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
