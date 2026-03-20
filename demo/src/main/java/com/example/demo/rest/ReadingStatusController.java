package com.example.demo.rest;


import com.example.demo.dto.ReadingStatusDto;
import com.example.demo.dto.ReviewDto;
import com.example.demo.entity.ReadingStatus;
import com.example.demo.service.ReadingStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reading-status")
@RequiredArgsConstructor
public class ReadingStatusController {
    private final ReadingStatusService readingStatusService;

    @PostMapping
    public ResponseEntity<ReadingStatusDto> createReadingStatus(@RequestBody ReadingStatusDto payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(readingStatusService.createReadingStatus(payload));
    }

}
