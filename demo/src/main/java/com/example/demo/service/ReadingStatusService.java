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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingStatusService {

    private final ReadingStatusRepository readingStatusRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReadingStatusDto createReadingStatus(ReadingStatusDto request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + request.getBookId()));

        if (request.getCurrentStatus() == null) {
            throw new RuntimeException("Reading status cannot be null");
        }

        ReadingStatus readingStatus = new ReadingStatus();
        readingStatus.setUser(user);
        readingStatus.setBook(book);
        readingStatus.setCurrentStatus(request.getCurrentStatus());

        switch (request.getCurrentStatus()) {
            case WANT_TO_READ:
                readingStatus.setStartedAt(null);
                readingStatus.setFinishedAt(null);
                break;

            case CURRENTLY_READING:
                readingStatus.setStartedAt(request.getStartedAt() != null
                        ? request.getStartedAt() : LocalDate.now());
                readingStatus.setFinishedAt(null);
                break;

            case READ:
                readingStatus.setStartedAt(request.getStartedAt());
                readingStatus.setFinishedAt(request.getFinishedAt() != null
                        ? request.getFinishedAt() : LocalDate.now());
                break;

        }

        return mapToDto(readingStatusRepository.save(readingStatus));
    }

    public ReadingStatusDto getReadingStatusById(Long statusId) {
        return mapToDto(readingStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Reading status not found with ID: " + statusId)));
    }

    public Page<ReadingStatusDto> getAllReadingStatuses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return readingStatusRepository.findAll(pageable).map(this::mapToDto);
    }

    public List<ReadingStatusDto> getReadingStatusesByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return readingStatusRepository.findByUserId(userId).stream()
                .map(this::mapToDto).collect(Collectors.toList());
    }

    public List<ReadingStatusDto> getReadingStatusesByBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Book not found with ID: " + bookId);
        }
        return readingStatusRepository.findByBookId(bookId).stream()
                .map(this::mapToDto).collect(Collectors.toList());
    }

    public ReadingStatusDto getReadingStatusForUserBook(Long userId, Long bookId) {
        ReadingStatus status = readingStatusRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new RuntimeException("Reading status not found for user: " + userId + " and book: " + bookId));
        return mapToDto(status);
    }

    //pending update bugs for state changes
    public ReadingStatusDto updateReadingStatus(Long statusId, ReadingStatusDto request) {
        ReadingStatus readingStatus = readingStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Reading status not found with ID: " + statusId));

        if (request.getCurrentStatus() != null) {
            readingStatus.setCurrentStatus(request.getCurrentStatus());
        }
        if (request.getStartedAt() != null) {
            readingStatus.setStartedAt(request.getStartedAt());
        }
        if (request.getFinishedAt() != null) {
            readingStatus.setFinishedAt(request.getFinishedAt());
        }

        return mapToDto(readingStatusRepository.save(readingStatus));
    }

    public void deleteReadingStatus(Long statusId) {
        if (!readingStatusRepository.existsById(statusId)) {
            throw new RuntimeException("Reading status not found with ID: " + statusId);
        }
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
}
