package com.example.demo.dto.response;

import java.time.LocalDateTime;

public class WorkResponseDto {
    private Long id;
    private String title;
    private String description;
    private String openLibraryWorkId;
    private Integer firstPublishYear;
    private String subtitle;
    private String subjects;
    private String authors;
    private String covers;
    private String identifiers;
    private Integer latestRevision;
    private Integer revision;
    private LocalDateTime created;
    private LocalDateTime lastModified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkResponseDto() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOpenLibraryWorkId() { return openLibraryWorkId; }
    public void setOpenLibraryWorkId(String openLibraryWorkId) { this.openLibraryWorkId = openLibraryWorkId; }

    public Integer getFirstPublishYear() { return firstPublishYear; }
    public void setFirstPublishYear(Integer firstPublishYear) { this.firstPublishYear = firstPublishYear; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }

    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }

    public String getCovers() { return covers; }
    public void setCovers(String covers) { this.covers = covers; }

    public String getIdentifiers() { return identifiers; }
    public void setIdentifiers(String identifiers) { this.identifiers = identifiers; }

    public Integer getLatestRevision() { return latestRevision; }
    public void setLatestRevision(Integer latestRevision) { this.latestRevision = latestRevision; }

    public Integer getRevision() { return revision; }
    public void setRevision(Integer revision) { this.revision = revision; }

    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
