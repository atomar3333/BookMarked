package com.example.demo.service;

import com.example.demo.dto.response.BookListResponseDto;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookList;
import com.example.demo.entity.Lists;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.BookListRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.ListsRepository;
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

@Service
@RequiredArgsConstructor
public class BookListService {

    private final BookListRepository bookListRepository;
    private final ListsRepository listsRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookListResponseDto addBookToList(Long listId, Long bookId) {
        Lists list = listsRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId));

        assertListOwnerOrAdmin(list);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));

        // Check if book already in list
        if (bookListRepository.findByListIdAndBookId(listId, bookId).isPresent()) {
            throw new RuntimeException("Book is already in this list");
        }

        BookList bookList = new BookList();
        bookList.setList(list);
        bookList.setBook(book);

        return mapToDto(bookListRepository.save(bookList));
    }

    @Transactional
    public void removeBookFromList(Long listId, Long bookId) {
        Lists list = listsRepository.findById(listId)
            .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId));
        assertListOwnerOrAdmin(list);

        BookList bookList = bookListRepository.findByListIdAndBookId(listId, bookId)
                .orElseThrow(() -> new RuntimeException("Book not found in this list"));

        bookListRepository.delete(bookList);
    }

    @Transactional(readOnly = true)
    public Page<BookListResponseDto> getBooksInList(Long listId, int page, int size) {
        Lists list = listsRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId));
        assertListVisibleToCurrentViewer(list);

        Pageable pageable = PageRequest.of(page, size);
        return bookListRepository.findByListId(listId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public long getBookCountInList(Long listId) {
        Lists list = listsRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId));
        assertListVisibleToCurrentViewer(list);

        return bookListRepository.countByListId(listId);
    }

    private BookListResponseDto mapToDto(BookList bookList) {
        BookListResponseDto dto = new BookListResponseDto();
        dto.setId(bookList.getId());
        dto.setListId(bookList.getList().getId());
        dto.setBookId(bookList.getBook().getId());
        dto.setAddedAt(bookList.getAddedAt());
        return dto;
    }

    private void assertListVisibleToCurrentViewer(Lists list) {
        if (!list.isPublic()) {
            User viewer = getCurrentUserOrThrow();
            boolean isAdmin = viewer.getRole() == Role.ROLE_ADMIN;
            if (!isAdmin && !viewer.getId().equals(list.getUser().getId())) {
                throw new AccessDeniedException("This list is private");
            }
        }
    }

    private User getCurrentUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        return userRepository.findByEmailId(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    private void assertListOwnerOrAdmin(Lists list) {
        User currentUser = getCurrentUserOrThrow();
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !currentUser.getId().equals(list.getUser().getId())) {
            throw new AccessDeniedException("You can only modify your own lists");
        }
    }
}
