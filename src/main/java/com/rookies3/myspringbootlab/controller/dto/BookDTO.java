package com.rookies3.myspringbootlab.controller.dto;

import com.rookies3.myspringbootlab.entity.Book;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

public class BookDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Book title is required")
        private String title;

        @NotBlank(message = "Author name is required")
        private String author;

        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+$",
                message = "ISBN must be valid (10 or 13 digits, with or without hyphens)")
        private String isbn;

        @PositiveOrZero(message = "Price must be positive or zero")
        private Integer price;

        @Past(message = "Publish date must be in the past")
        private LocalDate publishDate;

        // 여기를 수정합니다: 요청에서 "publisher"로 받았으면 이름을 "publisher"로 하거나,
        // 요청 데이터를 "publisherId"로 보내는 것으로 변경해야 합니다.
        // 현재 요청 데이터에 맞춰 "publisher"로 명명하되, 실제로는 ID를 받을 것이므로 Long 타입으로 합니다.
        @NotNull(message = "Publisher ID is required") // 필수값으로 설정
        private Long publisher; // 요청 데이터에 맞춰 'publisher'로 이름 지정

        @Valid
        private BookDetailDTO detailRequest;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatchRequest {
        private String title;
        private String author;
        @Pattern(regexp = "^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+$",
                message = "ISBN must be valid (10 or 13 digits, with or without hyphens)")
        private String isbn;
        @PositiveOrZero(message = "Price must be positive or zero")
        private Integer price;
        @Past(message = "Publish date must be in the past")
        private LocalDate publishDate;

        // PATCH 요청에서도 publisher 정보를 변경할 수 있도록 추가
        private Long publisher; // 요청 데이터에 맞춰 'publisher'로 이름 지정

        @Valid
        private BookDetailPatchRequest detailRequest;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookDetailDTO {
        private String description;
        private String language;
        private Integer pageCount;
        private String publisher;
        private String coverImageUrl;
        private String edition;
    }

    // BookDetail 부분 수정을 위한 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookDetailPatchRequest {
        private String description;
        private String language;
        private Integer pageCount;
        private String publisher;
        private String coverImageUrl;
        private String edition;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String author;
        private String isbn;
        private Integer price;
        private LocalDate publishDate;
        private PublisherDTO.SimpleResponse publisher; // 추가된 부분
        private BookDetailResponse detail;

        public static Response fromEntity(Book book) {
            BookDetailResponse detailResponse = book.getBookDetail() != null
                    ? BookDetailResponse.builder()
                    .id(book.getBookDetail().getId())
                    .description(book.getBookDetail().getDescription())
                    .language(book.getBookDetail().getLanguage())
                    .pageCount(book.getBookDetail().getPageCount())
                    .publisher(book.getBookDetail().getPublisher())
                    .coverImageUrl(book.getBookDetail().getCoverImageUrl())
                    .edition(book.getBookDetail().getEdition())
                    .build()
                    : null;

            // Publisher 정보 변환 (null 체크 추가)
            PublisherDTO.SimpleResponse publisherResponse = book.getPublisher() != null ?
                    PublisherDTO.SimpleResponse.fromEntity(book.getPublisher()) : null;

            return Response.builder()
                    .id(book.getId())
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .isbn(book.getIsbn())
                    .price(book.getPrice())
                    .publishDate(book.getPublishDate())
                    .publisher(publisherResponse) // 추가된 부분
                    .detail(detailResponse)
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimpleResponse {
        private Long id;
        private String title;
        private String author;
        private String isbn;
        private Integer price;
        private LocalDate publishDate;

        public static SimpleResponse fromEntity(Book book) {
            return SimpleResponse.builder()
                    .id(book.getId())
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .isbn(book.getIsbn())
                    .price(book.getPrice())
                    .publishDate(book.getPublishDate())
                    .build();
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookDetailResponse {
        private Long id;
        private String description;
        private String language;
        private Integer pageCount;
        private String publisher;
        private String coverImageUrl;
        private String edition;
    }
}