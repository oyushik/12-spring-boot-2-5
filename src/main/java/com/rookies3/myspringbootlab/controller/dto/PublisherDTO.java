package com.rookies3.myspringbootlab.controller.dto;

import com.rookies3.myspringbootlab.entity.Publisher;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class PublisherDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        private String name;

        @NotNull(message = "Established date is required")
        @PastOrPresent(message = "Established date must be in the past or present")
        private LocalDate establishedDate;

        @NotNull(message = "Address is required")
        @Size(max = 100, message = "Address cannot exceed 100 characters")
        private String address;
    }

    // patch 부분 수정을 위한 dto
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatchRequest {
        private String name;

        @PastOrPresent(message = "Established date must be in the past or present")
        private LocalDate establishedDate;

        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private LocalDate establishedDate;
        private String address;
        private Long bookCount;
        private List<BookDTO.SimpleResponse> books;

        public static Response fromEntity(Publisher publisher) {
            return Response.builder()
                    .id(publisher.getId())
                    .name(publisher.getName())
                    .establishedDate(publisher.getEstablishedDate())
                    .address(publisher.getAddress())
                    .bookCount((long) publisher.getBooks().size())
                    .books(publisher.getBooks().stream()
                            //.map(student -> StudentDTO.SimpleResponse.fromEntity(student))
                            .map(BookDTO.SimpleResponse::fromEntity)
                            .toList())
                    .build();
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimpleResponse {
        private Long id;
        private String name;
        private LocalDate establishedDate;
        private String address;
        private Long bookCount;

        public static SimpleResponse fromEntity(Publisher publisher) {
            return SimpleResponse.builder()
                    .id(publisher.getId())
                    .name(publisher.getName())
                    .establishedDate(publisher.getEstablishedDate())
                    .address(publisher.getAddress())
                    .bookCount((long) publisher.getBooks().size())
                    .build();
        }
    }
}