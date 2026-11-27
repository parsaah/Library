package com.example.library.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorrowingTest {

    @Test
    void shouldCreateBorrowingWithCorrectValues() {
        // Arrange
        Book book = createBook();
        String borrowerName = "John Doe";

        // Act
        Borrowing borrowing = new Borrowing(book, borrowerName);

        // Assert
        assertThat(borrowing.getBook()).isEqualTo(book);
        assertThat(borrowing.getBorrowerName()).isEqualTo(borrowerName);
    }

    @Test
    void shouldBeNotReturnedWhenCreated() {
        // Arrange
        Borrowing borrowing = new Borrowing(createBook(), "John Doe");

        // Act & Assert
        assertThat(borrowing.getReturnedAt()).isNull();
        assertThat(borrowing.isReturned()).isFalse();
    }

    @Test
    void shouldReturnBorrowingSuccessfully() {
        // Arrange
        Borrowing borrowing = new Borrowing(createBook(), "John Doe");

        // Act
        borrowing.returnBook();

        // Assert
        assertThat(borrowing.getReturnedAt()).isNotNull();
        assertThat(borrowing.isReturned()).isTrue();
    }

    @Test
    void shouldSetReturnedAtAfterReturning() {
        // Arrange
        Borrowing borrowing = new Borrowing(createBook(), "John Doe");

        // Act
        borrowing.returnBook();

        // Assert
        assertThat(borrowing.getReturnedAt()).isNotNull();
        assertThat(borrowing.getReturnedAt())
                .isAfterOrEqualTo(borrowing.getBorrowedAt());
    }

    @Test
    void shouldNotReturnBorrowingTwice() {
        // Arrange
        Borrowing borrowing = new Borrowing(createBook(), "John Doe");
        borrowing.returnBook();

        // Act & Assert
        assertThatThrownBy(borrowing::returnBook)
                .isInstanceOf(IllegalStateException.class);
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