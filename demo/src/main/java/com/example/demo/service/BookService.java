package com.example.demo.service;

import com.example.demo.dto.AddBookDto;
import com.example.demo.entity.Book;
import com.example.demo.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public Book createBook(AddBookDto request) {
        Book book = new Book();
        book.setGoogleBooksId(request.getGoogleBooksId());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCoverImageUrl(request.getCoverImageUrl());
        book.setDescription(request.getDescription());
        book.setPublishDate(request.getPublishDate());

        return bookRepository.save(book);
    }

    public Book getBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));
    }

    public Page<Book> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAll(pageable);
    }

    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> searchBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    public Book updateBook(Long bookId, AddBookDto request) {
        Book book = getBookById(bookId);
        book.setGoogleBooksId(request.getGoogleBooksId());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCoverImageUrl(request.getCoverImageUrl());
        book.setDescription(request.getDescription());
        book.setPublishDate(request.getPublishDate());

        return bookRepository.save(book);
    }

    public void deleteBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Book not found with ID: " + bookId);
        }
        bookRepository.deleteById(bookId);
    }
}
