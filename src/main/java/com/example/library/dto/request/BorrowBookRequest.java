package com.example.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BorrowBookRequest(

        @NotBlank
        @Size(max = 255)
        String borrowerName

) {
}