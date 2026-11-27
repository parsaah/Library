package com.example.library.service;

import com.example.library.dto.request.BookCreateRequest;
import com.example.library.dto.request.BookUpdateRequest;
import com.example.library.dto.response.BookResponse;
import com.example.library.entity.Book;
import com.example.library.exception.BusinessRuleViolationException;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.BookMapper;
import com.example.library.repository.BookRepository;
import com.example.library.specification.BookSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    // Create
    @Transactional
    public BookResponse createBook(BookCreateRequest request) {

        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateResourceException(
                    "A book with this ISBN already exists"
            );
        }

        Book book = new Book(
                request.title(),
                request.author(),
                request.isbn(),
                request.publicationYear()
        );

        Book savedBook = bookRepository.save(book);

        return bookMapper.toResponse(savedBook);
    }

    // Get by ID
    public BookResponse getBookById(Long id) {

        Book book = findActiveBookById(id);

        return bookMapper.toResponse(book);
    }

    // Search + Filter + Pagination + Sorting
    public Page<BookResponse> getAllBooks(
            String title,
            String author,
            Integer publicationYear,
            Boolean available,
            Pageable pageable
    ) {

        Specification<Book> specification =
                BookSpecification.filter(
                        title,
                        author,
                        publicationYear,
                        available
                );

        Page<Book> books = bookRepository.findAll(
                specification,
                pageable
        );

        return books.map(bookMapper::toResponse);
    }

    // Update
    @Transactional
    public BookResponse updateBook(
            Long id,
            BookUpdateRequest request
    ) {

        Book book = findActiveBookById(id);

        book.updateDetails(
                request.title(),
                request.author(),
                request.publicationYear()
        );

        return bookMapper.toResponse(book);
    }

    // Soft Delete
    @Transactional
    public void deleteBook(Long id) {

        Book book = findActiveBookById(id);

        if (!book.isAvailable()) {
            throw new BusinessRuleViolationException(
                    "Cannot delete a book that is currently borrowed"
            );
        }

        book.softDelete();
    }

    private Book findActiveBookById(Long id) {

        return bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id: " + id
                        )
                );
    }
}