package com.example.demo.service;

import com.example.demo.dto.GenreDto;
import com.example.demo.entity.Book;
import com.example.demo.entity.Genre;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.GenreRepository;
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
public class GenreService {

    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;

    @Transactional
    public GenreDto createGenre(GenreDto request) {
        if (request.getGenreName() == null || request.getGenreName().isBlank()) {
            throw new RuntimeException("Genre name cannot be empty");
        }

        Genre genre = new Genre();
        genre.setGenreName(request.getGenreName());

        return mapToDto(genreRepository.save(genre));
    }

    @Transactional(readOnly = true)
    public GenreDto getGenreById(Long genreId) {
        return mapToDto(genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Genre not found with ID: " + genreId)));
    }

    @Transactional(readOnly = true)
    public Page<GenreDto> getAllGenres(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return genreRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<GenreDto> searchGenres(String name) {
        return genreRepository.findByGenreNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public GenreDto updateGenre(Long genreId, GenreDto request) {
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Genre not found with ID: " + genreId));

        if (request.getGenreName() != null && !request.getGenreName().isBlank()) {
            genre.setGenreName(request.getGenreName());
        }

        return mapToDto(genreRepository.save(genre));
    }

    @Transactional
    public void deleteGenre(Long genreId) {
        if (!genreRepository.existsById(genreId)) {
            throw new RuntimeException("Genre not found with ID: " + genreId);
        }
        genreRepository.deleteById(genreId);
    }

    @Transactional
    public void linkGenreToBook(Long genreId, Long bookId) {
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Genre not found with ID: " + genreId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));

        if (book.getGenres().contains(genre)) {
            throw new RuntimeException("Genre is already linked to this book");
        }

        book.getGenres().add(genre);
        bookRepository.save(book);
    }

    @Transactional
    public void unlinkGenreFromBook(Long genreId, Long bookId) {
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Genre not found with ID: " + genreId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));

        if (!book.getGenres().contains(genre)) {
            throw new RuntimeException("Genre is not linked to this book");
        }

        book.getGenres().remove(genre);
        bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public List<GenreDto> getGenresByBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));
        return book.getGenres().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Book> getBooksByGenre(Long genreId) {
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Genre not found with ID: " + genreId));
        return genre.getBooks();
    }

    private GenreDto mapToDto(Genre genre) {
        GenreDto dto = new GenreDto();
        dto.setId(genre.getId());
        dto.setGenreName(genre.getGenreName());
        return dto;
    }
}
