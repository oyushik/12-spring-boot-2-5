package com.rookies3.myspringbootlab.repository;

import com.rookies3.myspringbootlab.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByPublisherId(Long publisherId);

    boolean existsByIsbn(String isbn);

    // PublisherService에서 사용하는 메서드
    Long countByPublisherId(Long publisherId); // 다시 추가됨

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.bookDetail WHERE b.id = :id")
    Optional<Book> findByIdWithBookDetail(@Param("id") Long id);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.bookDetail WHERE b.isbn = :isbn")
    Optional<Book> findByIsbnWithBookDetail(@Param("isbn") String isbn);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.bookDetail LEFT JOIN FETCH b.publisher WHERE b.id = :id")
    Optional<Book> findByIdWithAllDetalis(@Param("id") Long id);
}