package com.example.library.specification;

import com.example.library.entity.Book;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> hasTitle(String title) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"
                );
    }

    public static Specification<Book> hasAuthor(String author) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("author")),
                        "%" + author.toLowerCase() + "%"
                );
    }

    public static Specification<Book> hasPublicationYear(Integer publicationYear) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("publicationYear"),
                        publicationYear
                );
    }

    public static Specification<Book> isAvailable(Boolean available) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("available"),
                        available
                );
    }

    public static Specification<Book> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("deleted"));
    }

    public static Specification<Book> filter(
            String title,
            String author,
            Integer publicationYear,
            Boolean available
    ) {
        List<Specification<Book>> specifications = new ArrayList<>();

        specifications.add(isNotDeleted());

        if (title != null && !title.isBlank()) {
            specifications.add(hasTitle(title));
        }

        if (author != null && !author.isBlank()) {
            specifications.add(hasAuthor(author));
        }

        if (publicationYear != null) {
            specifications.add(hasPublicationYear(publicationYear));
        }

        if (available != null) {
            specifications.add(isAvailable(available));
        }

        return Specification.allOf(specifications);
    }
}