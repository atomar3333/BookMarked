package com.example.demo.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(BookAuthorId.class)
@Table(name = "book_author", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"book_id", "author_id"})
})
@Data
public class BookAuthor{
    @Id // Placed directly on the relationship!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Id // Placed directly on the relationship!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;
}
