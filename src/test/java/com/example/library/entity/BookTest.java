
package com.example.library.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookTest {

    @Test
    void shouldCreateBookWithCorrectValues() {
        // Arrange
        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008
        );

        // Assert
        assertThat(book.getTitle()).isEqualTo("Clean Code");
        assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(book.getIsbn()).isEqualTo("9780132350884");
        assertThat(book.getPublicationYear()).isEqualTo(2008);
        assertThat(book.isAvailable()).isTrue();
        assertThat(book.isDeleted()).isFalse();
    }

    @Test
    void shouldMarkBookAsBorrowed() {
        // Arrange
        Book book = createBook();

        // Act
        book.markAsBorrowed();

        // Assert
        assertThat(book.isAvailable()).isFalse();
    }

    @Test
    void shouldNotBorrowUnavailableBook() {
        // Arrange
        Book book = createBook();
        book.markAsBorrowed();

        // Act & Assert
        assertThatThrownBy(book::markAsBorrowed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Book is not available for borrowing");
    }

    @Test
    void shouldMarkBookAsReturned() {
        // Arrange
        Book book = createBook();
        book.markAsBorrowed();

        // Act
        book.markAsReturned();

        // Assert
        assertThat(book.isAvailable()).isTrue();
    }

    @Test
    void shouldNotReturnAvailableBook() {
        // Arrange
        Book book = createBook();

        // Act & Assert
        assertThatThrownBy(book::markAsReturned)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Book is already available");
    }

    @Test
    void shouldUpdateBookDetails() {
        // Arrange
        Book book = createBook();

        // Act
        book.updateDetails(
                "Effective Java",
                "Joshua Bloch",
                2018
        );

        // Assert
        assertThat(book.getTitle()).isEqualTo("Effective Java");
        assertThat(book.getAuthor()).isEqualTo("Joshua Bloch");
        assertThat(book.getPublicationYear()).isEqualTo(2018);

        // ISBN must remain unchanged
        assertThat(book.getIsbn()).isEqualTo("9780132350884");
    }

    @Test
    void shouldSoftDeleteAvailableBook() {
        // Arrange
        Book book = createBook();

        // Act
        book.softDelete();

        // Assert
        assertThat(book.isDeleted()).isTrue();
        assertThat(book.isAvailable()).isFalse();
    }

    @Test
    void shouldNotSoftDeleteBorrowedBook() {
        // Arrange
        Book book = createBook();
        book.markAsBorrowed();

        // Act & Assert
        assertThatThrownBy(book::softDelete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot delete a borrowed book");
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
