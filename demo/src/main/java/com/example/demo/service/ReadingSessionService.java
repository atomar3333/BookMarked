package com.example.demo.service;

import com.example.demo.dto.request.CreateReadingSessionRequestDto;
import com.example.demo.dto.request.UpdateReadingSessionRequestDto;
import com.example.demo.dto.response.ReadingSessionResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ReadProgressRepository;
import com.example.demo.repository.ReadingSessionRepository;
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
public class ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;
    private final ReadProgressRepository readProgressRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReadingSessionResponseDto createReadingSession(CreateReadingSessionRequestDto request) {
        assertSelfOrAdmin(request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        ReadProgress rp = readProgressRepository.findById(request.getReadProgressId())
                .orElseThrow(() -> new RuntimeException("ReadProgress not found with ID: " + request.getReadProgressId()));

        ReadingSession session = new ReadingSession();
        session.setUser(user);
        session.setReadProgress(rp);
        session.setDurationMinutes(request.getDurationMinutes());
        session.setStartPosition(request.getStartPosition());
        session.setEndPosition(request.getEndPosition());
        session.setSessionDate(request.getSessionDate());
        session.setNotes(request.getNotes());

        // Auto-update the read progress current position if end position is provided
        if (request.getEndPosition() != null) {
            rp.setCurrentPosition(request.getEndPosition());
            readProgressRepository.save(rp);
        }

        ReadingSession saved = readingSessionRepository.save(session);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public ReadingSessionResponseDto getReadingSessionById(Long id) {
        return readingSessionRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("ReadingSession not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<ReadingSessionResponseDto> getSessionsByReadProgress(Long readProgressId) {
        if (!readProgressRepository.existsById(readProgressId)) {
            throw new RuntimeException("ReadProgress not found with ID: " + readProgressId);
        }
        return readingSessionRepository.findByReadProgressId(readProgressId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReadingSessionResponseDto> getSessionsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return readingSessionRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReadingSessionResponseDto updateReadingSession(Long id, UpdateReadingSessionRequestDto request) {
        ReadingSession session = readingSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReadingSession not found with ID: " + id));

        assertSessionOwnerOrAdmin(session);

        if (request.getDurationMinutes() != null) session.setDurationMinutes(request.getDurationMinutes());
        if (request.getStartPosition() != null) session.setStartPosition(request.getStartPosition());
        if (request.getEndPosition() != null) session.setEndPosition(request.getEndPosition());
        if (request.getSessionDate() != null) session.setSessionDate(request.getSessionDate());
        if (request.getNotes() != null) session.setNotes(request.getNotes());

        ReadingSession saved = readingSessionRepository.save(session);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteReadingSession(Long id) {
        ReadingSession session = readingSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReadingSession not found with ID: " + id));

        assertSessionOwnerOrAdmin(session);
        readingSessionRepository.deleteById(id);
    }

    // ---------- Helper methods ----------

    private ReadingSessionResponseDto mapToDto(ReadingSession s) {
        ReadingSessionResponseDto dto = new ReadingSessionResponseDto();
        dto.setId(s.getId());
        dto.setUserId(s.getUser().getId());
        dto.setReadProgressId(s.getReadProgress().getId());
        dto.setDurationMinutes(s.getDurationMinutes());
        dto.setStartPosition(s.getStartPosition());
        dto.setEndPosition(s.getEndPosition());
        dto.setSessionDate(s.getSessionDate());
        dto.setNotes(s.getNotes());
        dto.setCreatedAt(s.getCreatedAt());
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
            throw new AccessDeniedException("You can only create reading sessions for your own account");
        }
    }

    private void assertSessionOwnerOrAdmin(ReadingSession session) {
        User current = getCurrentUserOrThrow();
        boolean isAdmin = current.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !current.getId().equals(session.getUser().getId())) {
            throw new AccessDeniedException("You can only modify your own reading sessions");
        }
    }
}
