package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.demo.dto.response.LikeStatsResponseDto;

@Entity
@Table(name = "books")
@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_books_id", unique = true, nullable = true)
    private String googleBooksId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "isbn", unique = true)
    private String isbn;

    @Column(name = "cover_image_url", length = 255)
    private String coverImageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Transient
    private LikeStatsResponseDto likeStats;

    @ManyToMany
    @JoinTable(
        name = "book_author",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<Author> authors = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "book_genre",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres = new ArrayList<>();

   // @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<com.example.demo.entity.Review> reviews;

    //@OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<com.example.demo.entity.ReadingStatus> readingStatuses;
}
