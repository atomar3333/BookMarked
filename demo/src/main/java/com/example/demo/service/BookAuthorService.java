package com.example.demo.service;

import com.example.demo.dto.response.BookAuthorResponseDto;
import com.example.demo.entity.BookAuthor;
import com.example.demo.entity.Author;
import com.example.demo.entity.Book;
import com.example.demo.repository.BookAuthorRepository;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookAuthorService {

    @Autowired
    private BookAuthorRepository bookAuthorRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AuthorRepository authorRepository;

    // Changed return type from boolean to BookAuthorResponseDto
    @Transactional
    public BookAuthorResponseDto addBookAuthor(Long bookId, Long authorId) {

        // 1. Extract the actual entities or throw an error immediately
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book id not in database: " + bookId));

        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author id not in database: " + authorId));

        // 2. Create the relationship entity
        BookAuthor bookAuthor = new BookAuthor();
        bookAuthor.setBook(book);
        bookAuthor.setAuthor(author);

        // 3. Save the actual entity
        bookAuthorRepository.save(bookAuthor);

        // 4. Map the raw entities to your DTO and return it
        return mapToDto(book, author);
    }

    @Transactional
    public boolean removeBookAuthor(Long bookId, Long authorId) {
        List<BookAuthor> links = bookAuthorRepository.findByBookIdAndAuthorId(bookId, authorId);
        if (links.isEmpty()) {
            return false;
        }
        bookAuthorRepository.deleteAll(links);
        return true;
    }

    public List<Long> getAuthorIdsForBook(Long bookId) {
        List<BookAuthor> links = bookAuthorRepository.findByBookId(bookId);
        return links.stream().map(link -> link.getAuthor().getId()).toList();
    }

    public List<Long> getBookIdsForAuthor(Long authorId) {
        List<BookAuthor> links = bookAuthorRepository.findByAuthorId(authorId);
        return links.stream().map(link -> link.getBook().getId()).toList();
    }

    private BookAuthorResponseDto mapToDto(Book book, Author author) {
        BookAuthorResponseDto dto = new BookAuthorResponseDto();
        dto.setBookId(book.getId());
        dto.setAuthorId(author.getId());
        return dto;
    }
}