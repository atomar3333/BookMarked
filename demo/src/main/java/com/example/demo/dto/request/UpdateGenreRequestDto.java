package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateGenreRequestDto {
    @Size(min = 1, max = 100, message = "Genre name must be between 1 and 100 characters")
    private String genreName;

    public UpdateGenreRequestDto() {}

    public String getGenreName() { return genreName; }
    public void setGenreName(String genreName) { this.genreName = genreName; }
}
