package com.example.demo.service;

import com.example.demo.dto.request.CreateReadProgressRequestDto;
import com.example.demo.dto.request.UpdateReadProgressRequestDto;
import com.example.demo.dto.response.ReadProgressResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.ReadProgressRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadProgressService {

    private final ReadProgressRepository readProgressRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    public ReadProgressResponseDto createReadProgress(CreateReadProgressRequestDto request) {
        assertSelfOrAdmin(request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + request.getBookId()));

        ReadProgress rp = new ReadProgress();
        rp.setUser(user);
        rp.setBook(book);
        rp.setCurrentPosition(request.getCurrentPosition() != null ? request.getCurrentPosition() : 1);
        rp.setTotalPages(request.getTotalPages());
        rp.setStatus(request.getStatus());
        rp.setStartedAt(request.getStartedAt());
        rp.setFinishedAt(request.getFinishedAt());
        rp.setReadNumber(request.getReadNumber() != null ? request.getReadNumber() : 1);

        ReadProgress saved = readProgressRepository.save(rp);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public ReadProgressResponseDto getReadProgressById(Long id) {
        return readProgressRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("ReadProgress not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<ReadProgressResponseDto> getReadProgressesByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return readProgressRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReadProgressResponseDto> getReadProgressesByBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Book not found with ID: " + bookId);
        }
        return readProgressRepository.findByBookId(bookId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReadProgressResponseDto> getReadProgressesForUserBook(Long userId, Long bookId) {
        return readProgressRepository.findByUserIdAndBookId(userId, bookId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReadProgressResponseDto updateReadProgress(Long id, UpdateReadProgressRequestDto request) {
        ReadProgress rp = readProgressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReadProgress not found with ID: " + id));

        assertReadProgressOwnerOrAdmin(rp);

        if (request.getCurrentPosition() != null) rp.setCurrentPosition(request.getCurrentPosition());
        if (request.getTotalPages() != null) rp.setTotalPages(request.getTotalPages());
        if (request.getStatus() != null) rp.setStatus(request.getStatus());
        if (request.getStartedAt() != null) rp.setStartedAt(request.getStartedAt());
        if (request.getFinishedAt() != null) rp.setFinishedAt(request.getFinishedAt());
        if (request.getReadNumber() != null) rp.setReadNumber(request.getReadNumber());

        ReadProgress saved = readProgressRepository.save(rp);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteReadProgress(Long id) {
        ReadProgress rp = readProgressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReadProgress not found with ID: " + id));

        assertReadProgressOwnerOrAdmin(rp);
        readProgressRepository.deleteById(id);
    }

    // ---------- Helper methods ----------

    private ReadProgressResponseDto mapToDto(ReadProgress rp) {
        ReadProgressResponseDto dto = new ReadProgressResponseDto();
        dto.setId(rp.getId());
        dto.setUserId(rp.getUser().getId());
        dto.setBookId(rp.getBook().getId());
        dto.setCurrentPosition(rp.getCurrentPosition());
        dto.setTotalPages(rp.getTotalPages());
        dto.setStatus(rp.getStatus());
        dto.setStartedAt(rp.getStartedAt());
        dto.setFinishedAt(rp.getFinishedAt());
        dto.setReadNumber(rp.getReadNumber());
        dto.setCreatedAt(rp.getCreatedAt());
        dto.setUpdatedAt(rp.getUpdatedAt());
        return dto;
    }

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByEmailId(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    private void assertSelfOrAdmin(Long targetUserId) {
        User current = getCurrentUserOrThrow();
        boolean isAdmin = current.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !current.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You can only create read progress for your own account");
        }
    }

    private void assertReadProgressOwnerOrAdmin(ReadProgress rp) {
        User current = getCurrentUserOrThrow();
        boolean isAdmin = current.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !current.getId().equals(rp.getUser().getId())) {
            throw new AccessDeniedException("You can only modify your own read progress");
        }
    }
}
