package com.example.demo.service;

import com.example.demo.dto.BookListDto;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookList;
import com.example.demo.entity.Lists;
import com.example.demo.repository.BookListRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.ListsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookListService {

    private final BookListRepository bookListRepository;
    private final ListsRepository listsRepository;
    private final BookRepository bookRepository;

    public BookListDto addBookToList(Long listId, Long bookId) {
        Lists list = listsRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId));

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

    public void removeBookFromList(Long listId, Long bookId) {
        BookList bookList = bookListRepository.findByListIdAndBookId(listId, bookId)
                .orElseThrow(() -> new RuntimeException("Book not found in this list"));

        bookListRepository.delete(bookList);
    }

    public Page<BookListDto> getBooksInList(Long listId, int page, int size) {
        if (!listsRepository.existsById(listId)) {
            throw new RuntimeException("List not found with ID: " + listId);
        }

        Pageable pageable = PageRequest.of(page, size);
        return bookListRepository.findByListId(listId, pageable).map(this::mapToDto);
    }

    public long getBookCountInList(Long listId) {
        if (!listsRepository.existsById(listId)) {
            throw new RuntimeException("List not found with ID: " + listId);
        }

        return bookListRepository.countByListId(listId);
    }

    private BookListDto mapToDto(BookList bookList) {
        BookListDto dto = new BookListDto();
        dto.setId(bookList.getId());
        dto.setListId(bookList.getList().getId());
        dto.setBookId(bookList.getBook().getId());
        dto.setAddedAt(bookList.getAddedAt());
        return dto;
    }
}
