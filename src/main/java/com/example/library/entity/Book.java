package com.example.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "books",
        indexes = {
                @Index(name = "idx_books_title", columnList = "title")
        }
)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(nullable = false, length = 20, unique = true)
    private String isbn;

    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public Book(
            String title,
            String author,
            String isbn,
            Integer publicationYear
    ) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateDetails(
            String title,
            String author,
            Integer publicationYear
    ) {
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
    }

    public void markAsBorrowed() {
        if (!available) {
            throw new IllegalStateException(
                    "Book is not available for borrowing"
            );
        }

        available = false;
    }

    public void markAsReturned() {
        if (available) {
            throw new IllegalStateException(
                    "Book is already available"
            );
        }

        available = true;
    }

    public void softDelete() {
        if (!available) {
            throw new IllegalStateException(
                    "Cannot delete a borrowed book"
            );
        }

        deleted = true;
        available = false;
    }
}