package com.rookies3.myspringbootlab.service;

import com.rookies3.myspringbootlab.controller.dto.BookDTO;
import com.rookies3.myspringbootlab.entity.Book;
import com.rookies3.myspringbootlab.entity.BookDetail;
import com.rookies3.myspringbootlab.entity.Publisher;
import com.rookies3.myspringbootlab.exception.BusinessException;
import com.rookies3.myspringbootlab.exception.ErrorCode;
import com.rookies3.myspringbootlab.repository.BookDetailRepository;
import com.rookies3.myspringbootlab.repository.BookRepository;
import com.rookies3.myspringbootlab.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final BookDetailRepository bookDetailRepository;
    private final PublisherRepository publisherRepository;

    public List<BookDTO.Response> getAllBooks() {
        // 모든 책을 가져올 때도 publisher 정보를 함께 로딩하도록 쿼리 변경이 필요할 수 있습니다.
        // 현재 findall()은 Lazy Loading으로 동작할 수 있어 N+1 문제가 발생할 수 있습니다.
        // 이 경우에는 findall() 대신 @Query를 통해 fetch join 하는 메서드를 추가하는 것이 좋습니다.
        return bookRepository.findAll()
                .stream()
                .map(BookDTO.Response::fromEntity)
                .toList();
    }

    public BookDTO.Response getBookById(Long id) {
        // findByIdWithAllDetalis로 변경하여 publisher와 bookDetail을 함께 가져옴
        Book book = bookRepository.findByIdWithAllDetalis(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id));
        return BookDTO.Response.fromEntity(book);
    }

    public BookDTO.Response getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbnWithBookDetail(isbn)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "ISBN", isbn));
        return BookDTO.Response.fromEntity(book);
    }

    public List<BookDTO.Response> getBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author)
                .stream()
                .map(BookDTO.Response::fromEntity)
                .toList();
    }

    public List<BookDTO.Response> getBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(BookDTO.Response::fromEntity)
                .toList();
    }

    public List<BookDTO.Response> getBooksByPublisherId(Long publisherId) {
        if (!publisherRepository.existsById(publisherId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Publisher", "id", publisherId);
        }
        // PublisherId로 조회할 때도 Publisher 정보가 포함되려면, 해당 쿼리도 fetch join으로 수정 필요
        return bookRepository.findByPublisherId(publisherId)
                .stream()
                .map(BookDTO.Response::fromEntity)
                .toList();
    }

    @Transactional
    public BookDTO.Response createBook(BookDTO.Request request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(ErrorCode.ISBN_DUPLICATE, request.getIsbn());
        }

        // 1. Publisher 조회 및 연결
        // request.getPublisher()는 요청 데이터의 "publisher": 2 에 해당하는 Long 값입니다.
        Publisher publisher = publisherRepository.findById(request.getPublisher())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Publisher", "id", request.getPublisher()));

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .price(request.getPrice())
                .publishDate(request.getPublishDate())
                .publisher(publisher) // 조회한 Publisher 엔티티를 Book에 설정
                .build();

        // 2. BookDetail 생성 및 연결 (기존 로직과 동일)
        if (request.getDetailRequest() != null) {
            BookDetail bookDetail = BookDetail.builder()
                    .description(request.getDetailRequest().getDescription())
                    .language(request.getDetailRequest().getLanguage())
                    .pageCount(request.getDetailRequest().getPageCount())
                    .publisher(request.getDetailRequest().getPublisher()) // BookDetail의 String publisher는 그대로 사용
                    .coverImageUrl(request.getDetailRequest().getCoverImageUrl())
                    .edition(request.getDetailRequest().getEdition())
                    .book(book)
                    .build();
            book.setBookDetail(bookDetail);
        }

        Book savedBook = bookRepository.save(book);
        return BookDTO.Response.fromEntity(savedBook); // fromEntity에서 publisher 정보도 변환해야 합니다.
    }

    @Transactional
    public BookDTO.Response updateBook(Long id, BookDTO.Request request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id));

        if (!book.getIsbn().equals(request.getIsbn()) &&
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(ErrorCode.ISBN_DUPLICATE, request.getIsbn());
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPrice(request.getPrice());
        book.setPublishDate(request.getPublishDate());

        // Publisher 업데이트 및 연결
        // request.getPublisher()는 업데이트 요청 데이터의 "publisher": 2 에 해당하는 Long 값입니다.
        Publisher newPublisher = publisherRepository.findById(request.getPublisher())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Publisher", "id", request.getPublisher()));
        book.setPublisher(newPublisher);


        if (request.getDetailRequest() != null) {
            BookDetail bookDetail = book.getBookDetail();

            if (bookDetail == null) {
                bookDetail = new BookDetail();
                bookDetail.setBook(book);
                book.setBookDetail(bookDetail);
            }

            bookDetail.setDescription(request.getDetailRequest().getDescription());
            bookDetail.setLanguage(request.getDetailRequest().getLanguage());
            bookDetail.setPageCount(request.getDetailRequest().getPageCount());
            bookDetail.setPublisher(request.getDetailRequest().getPublisher());
            bookDetail.setCoverImageUrl(request.getDetailRequest().getCoverImageUrl());
            bookDetail.setEdition(request.getDetailRequest().getEdition());
        }

        Book updatedBook = bookRepository.save(book);
        return BookDTO.Response.fromEntity(updatedBook);
    }

    @Transactional // 데이터 변경이 발생하므로 @Transactional 어노테이션 필요
    public BookDTO.Response partialUpdateBook(Long id, BookDTO.PatchRequest request) {
        // 1. 업데이트할 책을 ID로 조회합니다. 없으면 RESOURCE_NOT_FOUND 예외를 발생시킵니다.
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id));

        // 2. 요청 DTO의 각 필드를 확인하며, 값이 존재하는 경우에만 책 엔티티를 업데이트합니다.

        // 제목 (title) 업데이트
        if (request.getTitle() != null) {
            book.setTitle(request.getTitle());
        }

        // 저자 (author) 업데이트
        if (request.getAuthor() != null) {
            book.setAuthor(request.getAuthor());
        }

        // ISBN 업데이트 및 중복 검사
        if (request.getIsbn() != null) {
            // 현재 책의 ISBN과 다르고, 새로운 ISBN이 이미 존재한다면 중복 예외 발생
            if (!book.getIsbn().equals(request.getIsbn()) &&
                    bookRepository.existsByIsbn(request.getIsbn())) {
                throw new BusinessException(ErrorCode.ISBN_DUPLICATE, request.getIsbn());
            }
            book.setIsbn(request.getIsbn());
        }

        // 가격 (price) 업데이트
        if (request.getPrice() != null) {
            book.setPrice(request.getPrice());
        }

        // 출판일 (publishDate) 업데이트
        if (request.getPublishDate() != null) {
            book.setPublishDate(request.getPublishDate());
        }

        // 3. Publisher 부분 업데이트 및 연결
        // 요청에 'publisher' 필드 (Long 타입의 Publisher ID)가 있다면 해당 Publisher를 찾아서 연결합니다.
        if (request.getPublisher() != null) { // request.getPublisher()는 이제 Publisher ID (Long 값)입니다.
            // 요청된 publisher ID로 Publisher 엔티티를 조회합니다. 없으면 RESOURCE_NOT_FOUND 예외 발생.
            Publisher newPublisher = publisherRepository.findById(request.getPublisher())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Publisher", "id", request.getPublisher()));
            // 조회된 Publisher 엔티티를 Book에 설정하여 연관관계를 업데이트합니다.
            book.setPublisher(newPublisher);
        }

        // 4. BookDetail 부분 업데이트
        // 요청 DTO에 detailRequest가 있다면 BookDetail 정보를 업데이트합니다.
        if (request.getDetailRequest() != null) {
            BookDetail bookDetail = book.getBookDetail();

            // 만약 현재 책에 BookDetail이 없다면 새로 생성합니다.
            if (bookDetail == null) {
                bookDetail = new BookDetail();
                bookDetail.setBook(book); // 새로 생성된 BookDetail에 Book 연관관계 설정
                book.setBookDetail(bookDetail); // Book에도 BookDetail 연관관계 설정
            }

            // BookDetailPatchRequest 객체를 가져옵니다.
            BookDTO.BookDetailPatchRequest detailRequest = request.getDetailRequest();

            // 각 BookDetail 필드를 확인하며, 값이 존재하는 경우에만 업데이트합니다.
            if (detailRequest.getDescription() != null) {
                bookDetail.setDescription(detailRequest.getDescription());
            }
            if (detailRequest.getLanguage() != null) {
                bookDetail.setLanguage(detailRequest.getLanguage());
            }
            if (detailRequest.getPageCount() != null) {
                bookDetail.setPageCount(detailRequest.getPageCount());
            }
            if (detailRequest.getPublisher() != null) { // BookDetail의 String 타입 publisher 필드
                bookDetail.setPublisher(detailRequest.getPublisher());
            }
            if (detailRequest.getCoverImageUrl() != null) {
                bookDetail.setCoverImageUrl(detailRequest.getCoverImageUrl());
            }
            if (detailRequest.getEdition() != null) {
                bookDetail.setEdition(detailRequest.getEdition());
            }
        }

        // 5. 업데이트된 책 엔티티를 저장하고, 응답 DTO로 변환하여 반환합니다.
        Book updatedBook = bookRepository.save(book);
        return BookDTO.Response.fromEntity(updatedBook);
    }

    // BookDetail 만 업데이트 하는 메서드 (새로 추가)
    @Transactional
    public BookDTO.Response updateBookDetail(Long id, BookDTO.BookDetailPatchRequest request) {
        // Find the book
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id));

        BookDetail bookDetail = book.getBookDetail();

        // Create new detail if not exists
        if (bookDetail == null) {
            bookDetail = new BookDetail();
            bookDetail.setBook(book);
            book.setBookDetail(bookDetail);
        }

        // Update only provided fields
        if (request.getDescription() != null) {
            bookDetail.setDescription(request.getDescription());
        }
        if (request.getLanguage() != null) {
            bookDetail.setLanguage(request.getLanguage());
        }
        if (request.getPageCount() != null) {
            bookDetail.setPageCount(request.getPageCount());
        }
        if (request.getPublisher() != null) {
            bookDetail.setPublisher(request.getPublisher());
        }
        if (request.getCoverImageUrl() != null) {
            bookDetail.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getEdition() != null) {
            bookDetail.setEdition(request.getEdition());
        }

        // Save and return updated book
        Book updatedBook = bookRepository.save(book);
        return BookDTO.Response.fromEntity(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id);
        }
        bookRepository.deleteById(id);
    }
}