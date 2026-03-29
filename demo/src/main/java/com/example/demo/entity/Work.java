
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "works")
@Data
public class Work {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "open_library_work_id", unique = true)
    private String openLibraryWorkId;

    @Column(name = "first_publish_year")
    private Integer firstPublishYear;

    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String subjects;

    @Column(columnDefinition = "TEXT")
    private String authors;

    @Column(columnDefinition = "TEXT")
    private String covers;

    @Column(columnDefinition = "TEXT")
    private String identifiers;

    @Column(name = "latest_revision")
    private Integer latestRevision;

    private Integer revision;

    private LocalDateTime created;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and setters omitted for brevity
}
