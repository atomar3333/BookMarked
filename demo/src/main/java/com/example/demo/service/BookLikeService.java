package com.example.demo.service;

import com.example.demo.dto.LikeDto;
import com.example.demo.dto.LikeStatsDto;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookLike;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.BookLikeRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookLikeService {

    private final BookLikeRepository bookLikeRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    public LikeDto likeBook(Long bookId) {
        Long currentUserId = getCurrentUserIdOrThrow();
        
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + currentUserId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));

        if (bookLikeRepository.existsByUserIdAndBookId(currentUserId, bookId)) {
            throw new RuntimeException("User has already liked this book");
        }

        BookLike like = new BookLike();
        like.setUser(user);
        like.setBook(book);

        try {
            BookLike saved = bookLikeRepository.save(like);
            return mapToDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("User has already liked this book");
        }
    }

    @Transactional
    public void unlikeBook(Long bookId) {
        Long currentUserId = getCurrentUserIdOrThrow();
        
        BookLike like = bookLikeRepository.findByUserIdAndBookId(currentUserId, bookId)
                .orElseThrow(() -> new RuntimeException("Like not found"));
        bookLikeRepository.delete(like);
    }

    public Page<LikeDto> getBookLikes(Long bookId, int page, int size) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Book not found with ID: " + bookId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return bookLikeRepository.findByBookId(bookId, pageable).map(this::mapToDto);
    }

    public LikeStatsDto getBookLikeStats(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Book not found with ID: " + bookId);
        }
        
        Long likeCount = bookLikeRepository.countByBookId(bookId);
        
        Long currentUserId = getCurrentUserIdAttempt();
        Boolean likedByCurrentUser = false;
        if (currentUserId != null) {
            likedByCurrentUser = bookLikeRepository.existsByUserIdAndBookId(currentUserId, bookId);
        }
        
        return new LikeStatsDto(likeCount, likedByCurrentUser);
    }

    public boolean hasUserLikedBook(Long userId, Long bookId) {
        return bookLikeRepository.existsByUserIdAndBookId(userId, bookId);
    }

    private LikeDto mapToDto(BookLike like) {
        LikeDto dto = new LikeDto();
        dto.setId(like.getId());
        dto.setUserId(like.getUser().getId());
        dto.setUserName(like.getUser().getUserName());
        dto.setTargetId(like.getBook().getId());
        dto.setCreatedAt(like.getCreatedAt());
        return dto;
    }

    private Long getCurrentUserIdOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("User not authenticated");
        }
        String principal = authentication.getName();
        User user = userRepository.findByEmailId(principal)
            .or(() -> userRepository.findByUserName(principal))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    private Long getCurrentUserIdAttempt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        String principal = authentication.getName();
        return userRepository.findByEmailId(principal)
            .or(() -> userRepository.findByUserName(principal))
            .map(User::getId)
            .orElse(null);
    }
}
