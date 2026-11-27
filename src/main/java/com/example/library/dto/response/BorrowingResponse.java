package com.example.library.dto.response;

import java.time.LocalDateTime;

public record BorrowingResponse(
        Long id,
        Long bookId,
        String borrowerName,
        LocalDateTime borrowedAt,
        LocalDateTime returnedAt
) {
}