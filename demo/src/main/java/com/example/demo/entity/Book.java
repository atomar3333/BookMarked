package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(name = "google_books_id", unique = true)
    private String googleBooksId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author", nullable = false, length = 50)
    private String author;

    // Removed as it is not in the new schema reference
    // @Column(name = "isbn", unique = true)
    // private String isbn;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    // --- New Open Library Fields ---

    @Column(name = "open_library_work_id", length = 30)
    private String openLibraryWorkId;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "subjects", columnDefinition = "TEXT")
    private String subjects;

    // Mapped to DB column "authors".
    // Named 'authorsText' in Java to avoid conflict with the ManyToMany 'authors' list below.
    @Column(name = "authors", columnDefinition = "TEXT")
    private String authorsText;

    @Column(name = "covers", columnDefinition = "TEXT")
    private String covers;

    @Column(name = "identifiers", columnDefinition = "TEXT")
    private String identifiers;

    @Column(name = "latest_revision")
    private Integer latestRevision;

    @Column(name = "revision")
    private Integer revision;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    // --- Auto Timestamp Fields ---

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // --- Relationships & Transients ---

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
    // private List<com.example.demo.entity.Review> reviews;

    // @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<com.example.demo.entity.ReadingStatus> readingStatuses;
}