package com.example.library.concurrency;

import com.example.library.entity.Book;
import com.example.library.repository.BookRepository;

import jakarta.persistence.OptimisticLockException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookOptimisticLockingTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldAllowOnlyOneConcurrentUpdateUsingVersion() throws Exception {

        Book book = createAvailableBook();
        Long bookId = book.getId();

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        CountDownLatch bothRead = new CountDownLatch(2);
        CountDownLatch startUpdates = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<String> firstTransaction = executor.submit(() ->
                    executeConcurrentBorrow(
                            bookId,
                            transactionTemplate,
                            bothRead,
                            startUpdates
                    )
            );

            Future<String> secondTransaction = executor.submit(() ->
                    executeConcurrentBorrow(
                            bookId,
                            transactionTemplate,
                            bothRead,
                            startUpdates
                    )
            );

            // Wait until both transactions have read the same book
            assertTrue(
                    bothRead.await(10, TimeUnit.SECONDS),
                    "Both transactions should read the book"
            );

            // Allow both transactions to attempt their updates
            startUpdates.countDown();

            String firstResult = firstTransaction.get(15, TimeUnit.SECONDS);
            String secondResult = secondTransaction.get(15, TimeUnit.SECONDS);

            System.out.println("Transaction 1: " + firstResult);
            System.out.println("Transaction 2: " + secondResult);

            // Exactly one transaction must succeed
            assertEquals(
                    1,
                    countSuccesses(firstResult, secondResult)
            );

            // Exactly one transaction must fail due to optimistic locking
            assertEquals(
                    1,
                    countConflicts(firstResult, secondResult)
            );

            Book updatedBook = bookRepository
                    .findByIdAndDeletedFalse(bookId)
                    .orElseThrow();

            assertFalse(updatedBook.isAvailable());

            // Initial version = 0
            // One successful update = version 1
            assertEquals(1L, updatedBook.getVersion());

        } finally {
            startUpdates.countDown();
            executor.shutdownNow();

            bookRepository.deleteById(bookId);
        }
    }

    private String executeConcurrentBorrow(
            Long bookId,
            TransactionTemplate transactionTemplate,
            CountDownLatch bothRead,
            CountDownLatch startUpdates
    ) {

        try {
            transactionTemplate.execute(status -> {

                Book currentBook = bookRepository
                        .findByIdAndDeletedFalse(bookId)
                        .orElseThrow();

                // Both transactions must read before either updates
                bothRead.countDown();

                await(startUpdates);

                currentBook.markAsBorrowed();

                bookRepository.saveAndFlush(currentBook);

                return null;
            });

            return "SUCCESS";

        } catch (Exception exception) {

            if (isOptimisticLockConflict(exception)) {
                return "OPTIMISTIC_LOCK_CONFLICT";
            }

            throw new RuntimeException(
                    "Unexpected transaction failure",
                    exception
            );
        }
    }

    private boolean isOptimisticLockConflict(Throwable exception) {

        Throwable current = exception;

        while (current != null) {

            if (current instanceof OptimisticLockException
                    || current instanceof OptimisticLockingFailureException) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    private void await(CountDownLatch latch) {

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new RuntimeException(
                        "Timed out waiting for concurrent transactions"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread was interrupted",
                    exception
            );
        }
    }

    private int countSuccesses(String first, String second) {
        return (int) java.util.stream.Stream.of(first, second)
                .filter("SUCCESS"::equals)
                .count();
    }

    private int countConflicts(String first, String second) {
        return (int) java.util.stream.Stream.of(first, second)
                .filter("OPTIMISTIC_LOCK_CONFLICT"::equals)
                .count();
    }

    private Book createAvailableBook() {

        Book book = new Book(
                "Concurrency Test Book",
                "Test Author",
                "978-" + UUID.randomUUID().toString().substring(0, 10),
                2025
        );

        return bookRepository.saveAndFlush(book);
    }
}