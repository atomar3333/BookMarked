package com.example.demo.dto.response;

import lombok.Data;

@Data
public class BookAuthorResponseDto {
    private Long bookId;
    private Long authorId;
    private String bookTitle;
    private String authorName;
}
