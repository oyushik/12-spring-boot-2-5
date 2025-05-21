package com.rookies3.myspringbootlab.service;

import com.rookies3.myspringbootlab.controller.dto.PublisherDTO;
import com.rookies3.myspringbootlab.entity.Publisher;
import com.rookies3.myspringbootlab.exception.BusinessException;
import com.rookies3.myspringbootlab.exception.ErrorCode;
import com.rookies3.myspringbootlab.repository.BookRepository;
import com.rookies3.myspringbootlab.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;

    public List<PublisherDTO.SimpleResponse> getAllPublishers() {
        List<Publisher> departments = publisherRepository.findAll();

        return departments.stream()
                .map(publisher -> {
                    // 학생 수만 별도로 조회하여 students 컬렉션에 접근하지 않음
                    Long bookCount = bookRepository.countByPublisherId(publisher.getId());
                    return PublisherDTO.SimpleResponse.builder()
                            .id(publisher.getId())
                            .name(publisher.getName())
                            .establishedDate(publisher.getEstablishedDate())
                            .address(publisher.getAddress())
                            .bookCount(bookCount)
                            .build();
                })
                .toList();
    }

    public PublisherDTO.Response getPublisherById(Long id) {
        Publisher publisher = publisherRepository.findByIdWithBooks(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Publisher", "id", id));
        return PublisherDTO.Response.fromEntity(publisher);
    }

    public PublisherDTO.Response getPublisherByName(String name) {
        Publisher publisher = publisherRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Publisher", "name", name));
        return PublisherDTO.Response.fromEntity(publisher);
    }

    @Transactional
    public PublisherDTO.Response createPublisher(PublisherDTO.Request request) {
        if (publisherRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.PUBLISHER_NAME_DUPLICATE,
                    request.getName());
        }

        // --- 이 부분이 핵심 수정 사항입니다! ---
        Publisher publisher = Publisher.builder()
                .name(request.getName())
                .establishedDate(request.getEstablishedDate()) // <-- 추가
                .address(request.getAddress()) // <-- 추가
                .build();
        // ------------------------------------

        Publisher savedPublisher = publisherRepository.save(publisher);
        return PublisherDTO.Response.fromEntity(savedPublisher);
    }

    @Transactional
    public PublisherDTO.Response updatePublisher(Long id, PublisherDTO.Request request) {
        // Find the publisher
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Publisher", "id", id));

        // Check if another publisher already has the name
        if (!publisher.getName().equals(request.getName()) &&
                publisherRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.PUBLISHER_NAME_DUPLICATE,
                    request.getName());
        }

        // Update publisher info
        publisher.setName(request.getName());
        // updatePublisher에도 establishedDate와 address 업데이트 로직이 없으니 추가해주는게 좋습니다.
        publisher.setEstablishedDate(request.getEstablishedDate()); // <-- 추가
        publisher.setAddress(request.getAddress());                 // <-- 추가

        // Save and return updated publisher
        Publisher updatedPublisher = publisherRepository.save(publisher);
        return PublisherDTO.Response.fromEntity(updatedPublisher);
    }

    @Transactional
    public void deletePublisher(Long id) {
        if (!publisherRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Publisher", "id", id);
        }

        Long bookCount = bookRepository.countByPublisherId(id);
        if (bookCount > 0) {
            throw new BusinessException(ErrorCode.PUBLISHER_HAS_BOOKS,
                    id, bookCount);
        }

        publisherRepository.deleteById(id);
    }
}