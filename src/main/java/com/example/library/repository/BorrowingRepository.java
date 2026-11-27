package com.example.library.repository;

import com.example.library.entity.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    Optional<Borrowing> findByBook_IdAndReturnedAtIsNull(Long bookId);

    boolean existsByBook_IdAndReturnedAtIsNull(Long bookId);
}