package com.example.library.controller;

import com.example.library.dto.request.BorrowBookRequest;
import com.example.library.dto.response.BorrowingResponse;
import com.example.library.service.BorrowingService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books/{bookId}")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    // Borrow Book
    @PostMapping("/borrowings")
    public ResponseEntity<BorrowingResponse> borrowBook(
            @PathVariable Long bookId,
            @Valid @RequestBody BorrowBookRequest request
    ) {

        BorrowingResponse response = borrowingService.borrowBook(
                bookId,
                request.borrowerName()
        );

        return ResponseEntity
                .status(201)
                .body(response);
    }

    // Return Book
    @PutMapping("/return")
    public ResponseEntity<BorrowingResponse> returnBook(
            @PathVariable Long bookId
    ) {

        BorrowingResponse response =
                borrowingService.returnBook(bookId);

        return ResponseEntity.ok(response);
    }
}