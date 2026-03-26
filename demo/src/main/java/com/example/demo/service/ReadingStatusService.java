package com.example.demo.service;

import com.example.demo.dto.ReadingStatusDto;
import com.example.demo.entity.*;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.ReadingStatusRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingStatusService {

    private final ReadingStatusRepository readingStatusRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ActivityService activityService;

    @Transactional
    public ReadingStatusDto createReadingStatus(ReadingStatusDto request) {
        assertSelfOrAdmin(request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + request.getBookId()));

        if (request.getCurrentStatus() == null) {
            throw new RuntimeException("Reading status cannot be null");
        }

        ReadingStatus readingStatus = readingStatusRepository
                .findByUserIdAndBookId(request.getUserId(), request.getBookId())
                .orElseGet(ReadingStatus::new);
        ReadingStatusEnum previousStatus = readingStatus.getCurrentStatus();

        readingStatus.setUser(user);
        readingStatus.setBook(book);

        applyStatusRules(readingStatus, request.getCurrentStatus(), request.getStartedAt(), request.getFinishedAt());

        ReadingStatus saved = readingStatusRepository.save(readingStatus);
        activityService.record(user, ActivityType.READING_STATUS_UPDATED, saved.getId(), buildReadingStatusMetadata(book, previousStatus, saved.getCurrentStatus()));

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public ReadingStatusDto getReadingStatusById(Long statusId) {
        return mapToDto(readingStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Reading status not found with ID: " + statusId)));
    }

    @Transactional(readOnly = true)
    public Page<ReadingStatusDto> getAllReadingStatuses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return readingStatusRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<ReadingStatusDto> getReadingStatusesByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return readingStatusRepository.findByUserId(userId).stream()
                .map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReadingStatusDto> getReadingStatusesByBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Book not found with ID: " + bookId);
        }
        return readingStatusRepository.findByBookId(bookId).stream()
                .map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReadingStatusDto getReadingStatusForUserBook(Long userId, Long bookId) {
        ReadingStatus status = readingStatusRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new RuntimeException("Reading status not found for user: " + userId + " and book: " + bookId));
        return mapToDto(status);
    }

    //pending update option for changing start and finish date
    @Transactional
    public ReadingStatusDto updateReadingStatus(Long statusId, ReadingStatusDto request) {
        ReadingStatus readingStatus = readingStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Reading status not found with ID: " + statusId));

        assertReadingStatusOwnerOrAdmin(readingStatus);

        if (request == null) {
            throw new RuntimeException("Update payload cannot be null");
        }

        ReadingStatusEnum targetStatus = request.getCurrentStatus() != null
                ? request.getCurrentStatus()
                : readingStatus.getCurrentStatus();

        LocalDate nextStartedAt = request.getStartedAt() != null
                ? request.getStartedAt()
                : readingStatus.getStartedAt();

        LocalDate nextFinishedAt = request.getFinishedAt() != null
                ? request.getFinishedAt()
                : readingStatus.getFinishedAt();

        ReadingStatusEnum previousStatus = readingStatus.getCurrentStatus();

        applyStatusRules(readingStatus, targetStatus, nextStartedAt, nextFinishedAt);

        ReadingStatus saved = readingStatusRepository.save(readingStatus);
        activityService.record(saved.getUser(), ActivityType.READING_STATUS_UPDATED, saved.getId(), buildReadingStatusMetadata(saved.getBook(), previousStatus, saved.getCurrentStatus()));

        return mapToDto(saved);
    }

    @Transactional
    public void deleteReadingStatus(Long statusId) {
        ReadingStatus readingStatus = readingStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Reading status not found with ID: " + statusId));

        assertReadingStatusOwnerOrAdmin(readingStatus);
        readingStatusRepository.deleteById(statusId);
    }

    private ReadingStatusDto mapToDto(ReadingStatus readingStatus) {
        ReadingStatusDto dto = new ReadingStatusDto();
        dto.setId(readingStatus.getId());
        dto.setUserId(readingStatus.getUser().getId());
        dto.setBookId(readingStatus.getBook().getId());
        dto.setCurrentStatus(readingStatus.getCurrentStatus());
        dto.setStartedAt(readingStatus.getStartedAt());
        dto.setFinishedAt(readingStatus.getFinishedAt());
        return dto;
    }

    private void applyStatusRules(
            ReadingStatus readingStatus,
            ReadingStatusEnum targetStatus,
            LocalDate candidateStartedAt,
            LocalDate candidateFinishedAt
    ) {
        LocalDate nextStartedAt = candidateStartedAt;
        LocalDate nextFinishedAt = candidateFinishedAt;

        switch (targetStatus) {
            case WANT_TO_READ:
                nextStartedAt = null;
                nextFinishedAt = null;
                break;

            case CURRENTLY_READING:
                if (nextStartedAt == null) {
                    nextStartedAt = LocalDate.now();
                }
                nextFinishedAt = null;
                break;

            case READ:
                if (nextStartedAt == null) {
                    nextStartedAt = LocalDate.now();
                }
                if (nextFinishedAt == null) {
                    nextFinishedAt = LocalDate.now();
                }
                if (nextFinishedAt.isBefore(nextStartedAt)) {
                    throw new RuntimeException("Finish date cannot be before start date");
                }
                break;
        }

        readingStatus.setCurrentStatus(targetStatus);
        readingStatus.setStartedAt(nextStartedAt);
        readingStatus.setFinishedAt(nextFinishedAt);
    }

    private Map<String, Object> buildReadingStatusMetadata(Book book, ReadingStatusEnum oldStatus, ReadingStatusEnum newStatus) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("bookId", book.getId());
        metadata.put("bookTitle", book.getTitle());
        metadata.put("oldStatus", oldStatus != null ? oldStatus.name() : null);
        metadata.put("newStatus", newStatus != null ? newStatus.name() : null);
        return metadata;
    }

    private User getCurrentUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        return userRepository.findByEmailId(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    private void assertSelfOrAdmin(Long targetUserId) {
        User currentUser = getCurrentUserOrThrow();
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You can only create reading statuses for your own account");
        }
    }

    private void assertReadingStatusOwnerOrAdmin(ReadingStatus readingStatus) {
        User currentUser = getCurrentUserOrThrow();
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !currentUser.getId().equals(readingStatus.getUser().getId())) {
            throw new AccessDeniedException("You can only modify your own reading statuses");
        }
    }
}
