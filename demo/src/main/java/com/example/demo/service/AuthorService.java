package com.example.demo.service;

import com.example.demo.dto.request.CreateAuthorRequestDto;
import com.example.demo.dto.request.UpdateAuthorRequestDto;
import com.example.demo.dto.response.AuthorResponseDto;
import com.example.demo.entity.Author;
import com.example.demo.entity.Book;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Transactional
    public AuthorResponseDto createAuthor(CreateAuthorRequestDto request) {
        if (request.getAuthorName() == null || request.getAuthorName().isBlank()) {
            throw new RuntimeException("Author name cannot be empty");
        }

        Author author = new Author();
        author.setAuthorName(request.getAuthorName());
        author.setBio(request.getBio());
        author.setProfilePictureUrl(request.getProfilePictureUrl());

        return mapToDto(authorRepository.save(author));
    }

    @Transactional(readOnly = true)
    public AuthorResponseDto getAuthorById(Long authorId) {
        return mapToDto(authorRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + authorId)));
    }

    @Transactional(readOnly = true)
    public Page<AuthorResponseDto> getAllAuthors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return authorRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<AuthorResponseDto> searchAuthors(String name) {
        return authorRepository.findByAuthorNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AuthorResponseDto updateAuthor(Long authorId, UpdateAuthorRequestDto request) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + authorId));

        if (request.getAuthorName() != null && !request.getAuthorName().isBlank()) {
            author.setAuthorName(request.getAuthorName());
        }
        if (request.getBio() != null) {
            author.setBio(request.getBio());
        }
        if (request.getProfilePictureUrl() != null) {
            author.setProfilePictureUrl(request.getProfilePictureUrl());
        }

        return mapToDto(authorRepository.save(author));
    }

    @Transactional
    public void deleteAuthor(Long authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new RuntimeException("Author not found with ID: " + authorId);
        }
        authorRepository.deleteById(authorId);
    }

    @Transactional
    public void linkAuthorToBook(Long authorId, Long bookId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + authorId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));

        if (book.getAuthors().contains(author)) {
            throw new RuntimeException("Author is already linked to this book");
        }

        book.getAuthors().add(author);
        bookRepository.save(book);
    }

    @Transactional
    public void unlinkAuthorFromBook(Long authorId, Long bookId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + authorId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));

        if (!book.getAuthors().contains(author)) {
            throw new RuntimeException("Author is not linked to this book");
        }

        book.getAuthors().remove(author);
        bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public List<AuthorResponseDto> getAuthorsByBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));
        return book.getAuthors().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Book> getBooksByAuthor(Long authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + authorId));
        return author.getBooks();
    }

    private AuthorResponseDto mapToDto(Author author) {
        AuthorResponseDto dto = new AuthorResponseDto();
        dto.setId(author.getId());
        dto.setAuthorName(author.getAuthorName());
        dto.setBio(author.getBio());
        dto.setProfilePictureUrl(author.getProfilePictureUrl());
        return dto;
    }
}
