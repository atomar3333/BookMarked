package com.example.demo.dto.request;

import lombok.Data;

@Data
public class BookAuthorRequestDto {
    private Long bookId;
    private Long authorId;
}
