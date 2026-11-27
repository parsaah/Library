
package com.example.library.service;

import com.example.library.dto.response.BorrowingResponse;
import com.example.library.entity.Book;
import com.example.library.entity.Borrowing;
import com.example.library.exception.BusinessRuleViolationException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.BorrowingMapper;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowingRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BorrowingService {

    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;
    private final BorrowingMapper borrowingMapper;

    public BorrowingResponse borrowBook(
            Long bookId,
            String borrowerName
    ) {

        // 1. Find the book
        Book book = bookRepository.findByIdAndDeletedFalse(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id: " + bookId
                        )
                );

        // 2. Check availability
        if (!book.isAvailable()) {
            throw new BusinessRuleViolationException(
                    "This book is currently borrowed"
            );
        }

        // 3. Change book availability
        book.markAsBorrowed();

        // 4. Create borrowing record
        Borrowing borrowing = new Borrowing(book, borrowerName);

        // 5. Save borrowing record
        Borrowing savedBorrowing = borrowingRepository.save(borrowing);

        return borrowingMapper.toResponse(savedBorrowing);
    }

    public BorrowingResponse returnBook(Long bookId) {

        // 1. Find the active borrowing record
        Borrowing borrowing = borrowingRepository
                .findByBook_IdAndReturnedAtIsNull(bookId)
                .orElseThrow(() ->
                        new BusinessRuleViolationException(
                                "This book is not currently borrowed"
                        )
                );

        // 2. Get the associated book
        Book book = borrowing.getBook();

        // 3. Mark borrowing as returned
        borrowing.returnBook();

        // 4. Make the book available again
        book.markAsReturned();

        return borrowingMapper.toResponse(borrowing);
    }
}
