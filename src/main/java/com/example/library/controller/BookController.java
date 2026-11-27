package com.example.library.controller;

import com.example.library.dto.request.BookCreateRequest;
import com.example.library.dto.request.BookUpdateRequest;
import com.example.library.dto.response.BookResponse;
import com.example.library.service.BookService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // Create Book
    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookCreateRequest request
    ) {

        BookResponse response = bookService.createBook(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    // Get Book by ID
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(
            @PathVariable Long id
    ) {

        BookResponse response = bookService.getBookById(id);

        return ResponseEntity.ok(response);
    }

    // Search + Filter + Pagination + Sorting
    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer publicationYear,
            @RequestParam(required = false) Boolean available,

            @PageableDefault(
                    size = 10,
                    sort = "title"
            )
            Pageable pageable
    ) {

        Page<BookResponse> response = bookService.getAllBooks(
                title,
                author,
                publicationYear,
                available,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    // Update Book
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request
    ) {

        BookResponse response = bookService.updateBook(id, request);

        return ResponseEntity.ok(response);
    }

    // Soft Delete Book
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable Long id
    ) {

        bookService.deleteBook(id);

        return ResponseEntity.noContent().build();
    }
}