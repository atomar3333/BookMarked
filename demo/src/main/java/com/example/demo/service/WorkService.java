package com.example.demo.service;

import com.example.demo.dto.request.AddWorkRequestDto;
import com.example.demo.entity.Work;
import com.example.demo.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkService {

    private final WorkRepository workRepository;

    @Transactional
    public Work createWork(AddWorkRequestDto request) {
        Work work = new Work();
        work.setTitle(request.getTitle());
        work.setDescription(request.getDescription());
        work.setOpenLibraryWorkId(request.getOpenLibraryWorkId());
        work.setFirstPublishYear(request.getFirstPublishYear());
        work.setSubtitle(request.getSubtitle());
        work.setSubjects(request.getSubjects());
        work.setAuthors(request.getAuthors());
        work.setCovers(request.getCovers());
        work.setIdentifiers(request.getIdentifiers());
        work.setLatestRevision(request.getLatestRevision());
        work.setRevision(request.getRevision());
        work.setCreated(request.getCreated());
        work.setLastModified(request.getLastModified());
        work.setCreatedAt(request.getCreatedAt());
        work.setUpdatedAt(request.getUpdatedAt());
        return workRepository.save(work);
    }

    @Transactional(readOnly = true)
    public Work getWorkById(Long workId) {
        return workRepository.findById(workId)
                .orElseThrow(() -> new RuntimeException("Work not found with ID: " + workId));
    }

    @Transactional(readOnly = true)
    public Page<Work> getAllWorks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return workRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Work> searchWorksByTitle(String title) {
        return workRepository.findByTitleContainingIgnoreCase(title);
    }

    @Transactional(readOnly = true)
    public List<Work> searchWorksByAuthor(String author) {
        return workRepository.findByAuthorsContainingIgnoreCase(author);
    }

    @Transactional
    public Work updateWork(Long workId, AddWorkRequestDto request) {
        Work work = getWorkById(workId);
        work.setTitle(request.getTitle());
        work.setDescription(request.getDescription());
        work.setOpenLibraryWorkId(request.getOpenLibraryWorkId());
        work.setFirstPublishYear(request.getFirstPublishYear());
        work.setSubtitle(request.getSubtitle());
        work.setSubjects(request.getSubjects());
        work.setAuthors(request.getAuthors());
        work.setCovers(request.getCovers());
        work.setIdentifiers(request.getIdentifiers());
        work.setLatestRevision(request.getLatestRevision());
        work.setRevision(request.getRevision());
        work.setCreated(request.getCreated());
        work.setLastModified(request.getLastModified());
        work.setCreatedAt(request.getCreatedAt());
        work.setUpdatedAt(request.getUpdatedAt());
        return workRepository.save(work);
    }

    @Transactional
    public void deleteWork(Long workId) {
        if (!workRepository.existsById(workId)) {
            throw new RuntimeException("Work not found with ID: " + workId);
        }
        workRepository.deleteById(workId);
    }
}
