package com.example.demo.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class BookAuthorId implements Serializable {
    // These names MUST exactly match the variable names in the BookAuthor class
    private Long book;
    private Long author;
}