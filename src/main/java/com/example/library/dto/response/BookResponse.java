package com.example.library.dto.response;

import java.time.LocalDateTime;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        Integer publicationYear,
        boolean available,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}