package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "authors")
@Data
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_picture_url", length = 255)
    private String profilePictureUrl;

    @ManyToMany(mappedBy = "authors")
    @JsonIgnore
    private List<Book> books = new ArrayList<>();
}
