package com.example.demo.rest;

import com.example.demo.dto.CreateListDto;
import com.example.demo.dto.ListDto;
import com.example.demo.dto.UpdateListDto;
import com.example.demo.service.ListsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class ListsController {

    private final ListsService listsService;

    @PostMapping
    public ResponseEntity<ListDto> createList(@RequestBody CreateListDto payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listsService.createList(payload));
    }

    @GetMapping("/{listId}")
    public ResponseEntity<ListDto> getListById(@PathVariable Long listId) {
        try {
            return ResponseEntity.ok(listsService.getListById(listId));
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
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(@PathVariable Long listId) {
        try {
            listsService.deleteList(listId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
