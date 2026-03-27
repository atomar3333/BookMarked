package com.example.demo.rest;

import com.example.demo.dto.request.CreateUserRequestDto;
import com.example.demo.dto.request.UpdateUserProfileRequestDto;
import com.example.demo.dto.response.UserProfileResponseDto;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserProfileResponseDto registerUser(@Valid @RequestBody CreateUserRequestDto request) {
        return userService.createUser(request);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(@PathVariable Long userId) {
        try {
            UserProfileResponseDto user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getCurrentUserProfile() {
        try {
            return ResponseEntity.ok(userService.getCurrentUser());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<UserProfileResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserProfileResponseDto> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDto> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserProfileRequestDto request) {
        try {
            UserProfileResponseDto updatedUser = userService.updateUser(userId, request);
            return ResponseEntity.ok(updatedUser);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUserAccount(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponseDto>> searchUsers(@RequestParam String userName) {
        List<UserProfileResponseDto> users = userService.searchUsersByName(userName);
        return ResponseEntity.ok(users);
    }
}