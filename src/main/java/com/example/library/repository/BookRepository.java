package com.example.library.repository;

import com.example.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BookRepository extends
        JpaRepository<Book, Long>,
        JpaSpecificationExecutor<Book> {

    Optional<Book> findByIdAndDeletedFalse(Long id);

    boolean existsByIsbn(String isbn);
}