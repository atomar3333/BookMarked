package com.example.demo.dto.response;

public class GenreResponseDto {
    private Long id;
    private String genreName;

    public GenreResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGenreName() { return genreName; }
    public void setGenreName(String genreName) { this.genreName = genreName; }
}
