package com.example.library.service;

import com.example.library.dto.request.BookCreateRequest;
import com.example.library.dto.response.BookResponse;
import com.example.library.dto.request.BookUpdateRequest;
import com.example.library.entity.Book;
import com.example.library.exception.BusinessRuleViolationException;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.BookMapper;
import com.example.library.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldCreateBookSuccessfully() {
        // Arrange
        BookCreateRequest request = new BookCreateRequest(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008
        );

        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008
        );

        BookResponse response = new BookResponse(
                1L,
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008,
                true,
                null,
                null
        );

        when(bookRepository.existsByIsbn(request.isbn()))
                .thenReturn(false);

        when(bookRepository.save(any(Book.class)))
                .thenReturn(book);

        when(bookMapper.toResponse(book))
                .thenReturn(response);

        // Act
        BookResponse result = bookService.createBook(request);

        // Assert
        assertThat(result).isEqualTo(response);

        verify(bookRepository).existsByIsbn(request.isbn());
        verify(bookRepository).save(any(Book.class));
        verify(bookMapper).toResponse(book);
    }

    @Test
    void shouldThrowExceptionWhenIsbnAlreadyExists() {
        // Arrange
        BookCreateRequest request = new BookCreateRequest(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008
        );

        when(bookRepository.existsByIsbn(request.isbn()))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(bookRepository, never()).save(any(Book.class));
        verifyNoInteractions(bookMapper);
    }

    @Test
    void shouldGetBookByIdSuccessfully() {
        // Arrange
        Long bookId = 1L;

        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008
        );

        BookResponse response = new BookResponse(
                bookId,
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008,
                true,
                null,
                null
        );

        when(bookRepository.findByIdAndDeletedFalse(bookId))
                .thenReturn(java.util.Optional.of(book));

        when(bookMapper.toResponse(book))
                .thenReturn(response);

        // Act
        BookResponse result = bookService.getBookById(bookId);

        // Assert
        assertThat(result).isEqualTo(response);

        verify(bookRepository).findByIdAndDeletedFalse(bookId);
        verify(bookMapper).toResponse(book);
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        // Arrange
        Long bookId = 100L;

        when(bookRepository.findByIdAndDeletedFalse(bookId))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.getBookById(bookId))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(bookMapper);
    }

    @Test
    void shouldUpdateBookSuccessfully() {
        // Arrange
        Long bookId = 1L;

        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008
        );

        BookUpdateRequest request = new BookUpdateRequest(
                "Clean Code - Updated",
                "Robert C. Martin",
                2009
        );

        BookResponse response = new BookResponse(
                bookId,
                "Clean Code - Updated",
                "Robert C. Martin",
                "9780132350884",
                2009,
                true,
                null,
                null
        );

        when(bookRepository.findByIdAndDeletedFalse(bookId))
                .thenReturn(java.util.Optional.of(book));

        when(bookMapper.toResponse(book))
                .thenReturn(response);

        // Act
        BookResponse result = bookService.updateBook(bookId, request);

        // Assert
        assertThat(result).isEqualTo(response);
        assertThat(book.getTitle()).isEqualTo("Clean Code - Updated");
        assertThat(book.getPublicationYear()).isEqualTo(2009);
        assertThat(book.getIsbn()).isEqualTo("9780132350884");

        verify(bookRepository).findByIdAndDeletedFalse(bookId);
        verify(bookMapper).toResponse(book);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void shouldSoftDeleteBookSuccessfully() {
        // Arrange
        Long bookId = 1L;

        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008
        );

        when(bookRepository.findByIdAndDeletedFalse(bookId))
                .thenReturn(java.util.Optional.of(book));

        // Act
        bookService.deleteBook(bookId);

        // Assert
        assertThat(book.isDeleted()).isTrue();

        verify(bookRepository).findByIdAndDeletedFalse(bookId);
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void shouldNotDeleteBorrowedBook() {
        // Arrange
        Long bookId = 1L;

        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008
        );

        book.markAsBorrowed();

        when(bookRepository.findByIdAndDeletedFalse(bookId))
                .thenReturn(java.util.Optional.of(book));

        // Act & Assert
        assertThatThrownBy(() -> bookService.deleteBook(bookId))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(book.isDeleted()).isFalse();

        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void shouldReturnPagedBooks() {
        // Arrange
        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008
        );

        BookResponse response = new BookResponse(
                1L,
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                2008,
                true,
                null,
                null
        );

        org.springframework.data.domain.Page<Book> bookPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(book)
                );

        when(bookRepository.findAll(
                org.mockito.ArgumentMatchers.any(
                        org.springframework.data.jpa.domain.Specification.class
                ),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(bookPage);

        when(bookMapper.toResponse(book))
                .thenReturn(response);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        // Act
        org.springframework.data.domain.Page<BookResponse> result =
                bookService.getAllBooks(
                        "Clean",
                        "Martin",
                        2008,
                        true,
                        pageable
                );

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);

        verify(bookRepository).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                eq(pageable)
        );
    }
}