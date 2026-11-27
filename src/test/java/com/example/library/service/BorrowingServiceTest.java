package com.example.library.service;

import com.example.library.dto.response.BorrowingResponse;
import com.example.library.entity.Book;
import com.example.library.entity.Borrowing;
import com.example.library.exception.BusinessRuleViolationException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.BorrowingMapper;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private BorrowingMapper borrowingMapper;

    @InjectMocks
    private BorrowingService borrowingService;

    @Test
    void shouldBorrowBookSuccessfully() {
        // Arrange
        Long bookId = 1L;
        String borrowerName = "John Doe";

        Book book = createBook();

        Borrowing borrowing = new Borrowing(book, borrowerName);

        BorrowingResponse response = new BorrowingResponse(
                1L,
                bookId,
                borrowerName,
                null,
                null
        );

        when(bookRepository.findByIdAndDeletedFalse(bookId))
                .thenReturn(Optional.of(book));

        when(borrowingRepository.save(any(Borrowing.class)))
                .thenReturn(borrowing);

        when(borrowingMapper.toResponse(borrowing))
                .thenReturn(response);

        // Act
        BorrowingResponse result =
                borrowingService.borrowBook(bookId, borrowerName);

        // Assert
        assertThat(result).isEqualTo(response);
        assertThat(book.isAvailable()).isFalse();
        assertThat(borrowing.getBook()).isEqualTo(book);
        assertThat(borrowing.getBorrowerName()).isEqualTo(borrowerName);

        verify(bookRepository).findByIdAndDeletedFalse(bookId);
        verify(borrowingRepository).save(any(Borrowing.class));
        verify(borrowingMapper).toResponse(borrowing);
    }

    @Test
    void shouldNotBorrowUnavailableBook() {
        // Arrange
        Long bookId = 1L;
        Book book = createBook();

        book.markAsBorrowed();

        when(bookRepository.findByIdAndDeletedFalse(bookId))
                .thenReturn(Optional.of(book));

        // Act & Assert
        assertThatThrownBy(
                () -> borrowingService.borrowBook(bookId, "Jane Doe")
        )
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(borrowingRepository, never()).save(any(Borrowing.class));
        verifyNoInteractions(borrowingMapper);
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        // Arrange
        Long bookId = 100L;

        when(bookRepository.findByIdAndDeletedFalse(bookId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(
                () -> borrowingService.borrowBook(bookId, "John Doe")
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(borrowingRepository, never()).save(any(Borrowing.class));
        verifyNoInteractions(borrowingMapper);
    }

    @Test
    void shouldReturnBookSuccessfully() {
        // Arrange
        Long bookId = 1L;

        Book book = createBook();
        book.markAsBorrowed();

        Borrowing borrowing = new Borrowing(book, "John Doe");

        BorrowingResponse response = new BorrowingResponse(
                1L,
                bookId,
                "John Doe",
                null,
                null
        );

        when(borrowingRepository.findByBook_IdAndReturnedAtIsNull(bookId))
                .thenReturn(Optional.of(borrowing));

        when(borrowingMapper.toResponse(borrowing))
                .thenReturn(response);

        // Act
        BorrowingResponse result = borrowingService.returnBook(bookId);

        // Assert
        assertThat(result).isEqualTo(response);

        assertThat(borrowing.isReturned()).isTrue();
        assertThat(borrowing.getReturnedAt()).isNotNull();

        assertThat(book.isAvailable()).isTrue();

        verify(borrowingRepository)
                .findByBook_IdAndReturnedAtIsNull(bookId);

        verify(borrowingMapper).toResponse(borrowing);

        verify(borrowingRepository, never()).save(any(Borrowing.class));
    }

    @Test
    void shouldThrowExceptionWhenBookIsNotBorrowed() {
        // Arrange
        Long bookId = 1L;

        when(borrowingRepository.findByBook_IdAndReturnedAtIsNull(bookId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> borrowingService.returnBook(bookId))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(borrowingMapper, never()).toResponse(any(Borrowing.class));
    }

    private Book createBook() {
        return new Book(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008
        );
    }
}