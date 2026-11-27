package com.example.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "borrowings",
        indexes = {
                @Index(name = "idx_borrowings_book_id", columnList = "book_id")
        }
)
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrower_name", nullable = false, length = 255)
    private String borrowerName;

    @Column(name = "borrowed_at", nullable = false, updatable = false)
    private LocalDateTime borrowedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    public Borrowing(Book book, String borrowerName) {
        this.book = book;
        this.borrowerName = borrowerName;
        this.borrowedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (borrowedAt == null) {
            borrowedAt = LocalDateTime.now();
        }
    }

    public boolean isReturned() {
        return returnedAt != null;
    }

    public void returnBook() {
        if (isReturned()) {
            throw new IllegalStateException(
                    "This borrowing record is already returned"
            );
        }

        returnedAt = LocalDateTime.now();
    }
}