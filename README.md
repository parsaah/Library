# 📚 Library Management API

### A RESTful backend for managing books, borrowing, and returns.

![Java](https://img.shields.io/badge/Java-25%2B-orange?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring
Boot](https://img.shields.io/badge/Spring_Boot-brightgreen?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![REST
API](https://img.shields.io/badge/REST-API-6DB33F?style=for-the-badge)

------------------------------------------------------------------------

## ✨ Overview

Library Management API is a backend application built with **Java and
Spring Boot** to manage a library's book catalog and borrowing workflow.

The API supports book creation, searching, filtering, pagination,
sorting, updates, soft deletion, borrowing, and returning books. The
implementation focuses on clear separation of responsibilities,
validation, business rules, persistence, and testability.

## 🚀 Features

-   📖 Create, retrieve, update, and manage books
-   🔎 Search by title and author
-   🧩 Combine filters for title, author, publication year, and
    availability
-   📄 Pagination and sorting
-   🔄 Borrow and return books with business-rule enforcement
-   🛡️ Soft deletion instead of physical deletion
-   ⚠️ Centralized exception handling
-   ✅ Unit tests

## 🧰 Tech Stack

  Technology            Purpose
  --------------------- ----------------------------------------------
  **Java 25**          Main programming language
  **Spring Boot**       Application framework and REST API
  **Spring Web**        HTTP endpoints and request/response handling
  **Spring Data JPA**   Repository and persistence abstraction
  **Hibernate**         ORM and entity persistence
  **PostgreSQL**        Relational database
  **Bean Validation**   Request validation
  **MapStruct**         DTO/entity mapping
  **Lombok**            Reduce boilerplate code
  **Maven**             Dependency management and build
  **JUnit 5**           Automated tests
  **Mockito**           Unit-test mocking

## 🏁 Getting Started

### Prerequisites

Make sure the following tools are installed:

-   **JDK 25 or newer**
-   **Maven 3.8+** (or use the Maven Wrapper if included)
-   **PostgreSQL**
-   Git

### 1. Clone the repository

``` bash
git clone <repository-url>
cd <project-directory>
```

### 2. Create a PostgreSQL database

Create a database for the application, for example:

``` sql
CREATE DATABASE library;
```

### 3. Configure the application

Update the database configuration in
`src/main/resources/application.yml` (or `application.properties`) to
match your local PostgreSQL setup.

Example configuration:

``` yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/library
    username: YOUR_DB_USERNAME
    password: YOUR_DB_PASSWORD

  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
```

> **Important:** Replace the example credentials with your own. Never
> commit real credentials or secrets to version control.

### 4. Run the application

Using Maven:

``` bash
mvn spring-boot:run
```

Or build and run the executable JAR:

``` bash
mvn clean package
java -jar target/<application-name>.jar
```

Once started, the API will be available at:

``` text
http://localhost:8080
```

## 🧪 Running Tests

Run the complete test suite with:

``` bash
mvn test
```

The test suite covers key entity behavior, service-level business logic,
and repository persistence/query behavior.

> Repository integration tests require a PostgreSQL test database
> configured for the test environment.

## 🗂️ Project Structure

``` text
src
├── main
│   ├── java
│   │   └── com.example.library
│   │       ├── controller      # REST API endpoints
│   │       ├── service         # Application and business logic
│   │       ├── repository      # Spring Data repositories
│   │       ├── entity          # JPA entities and domain behavior
│   │       ├── dto
│   │       │   ├── request     # Incoming API payloads
│   │       │   └── response    # API response models
│   │       ├── mapper          # MapStruct mappers
│   │       ├── specification   # Dynamic search/filter specifications
│   │       └── exception       # Custom exceptions and global handler
│   └── resources
│       └── application.yml
└── test
    └── java
        └── com.example.library
            ├── entity
            ├── service
```

> Package names and paths may need to be adjusted to match the final
> repository layout.

## 🔌 API Endpoints

### Books

  -----------------------------------------------------------------------
  Method                  Endpoint                Description
  ----------------------- ----------------------- -----------------------
  `POST`                  `/api/books`            Create a book

  `GET`                   `/api/books`            List, search, filter,
                                                  paginate, and sort
                                                  books

  `GET`                   `/api/books/{id}`       Get a book by ID

  `PUT`                   `/api/books/{id}`       Update title, author,
                                                  and publication year

  `DELETE`                `/api/books/{id}`       Soft-delete a book
  -----------------------------------------------------------------------

### Borrowing

  ----------------------------------------------------------------------------------
  Method                  Endpoint                           Description
  ----------------------- ---------------------------------- -----------------------
  `POST`                  `/api/books/{bookId}/borrowings`   Borrow an available
                                                             book

  `PUT`                   `/api/books/{bookId}/return`       Return a currently
                                                             borrowed book
  ----------------------------------------------------------------------------------

### Query Parameters

The book listing endpoint supports combining the following filters:

  -------------------------------------------------------------------------
  Parameter               Description             Example
  ----------------------- ----------------------- -------------------------
  `title`                 Case-insensitive title  `?title=clean`
                          search                  

  `author`                Case-insensitive author `?author=martin`
                          search                  

  `publicationYear`       Filter by publication   `?publicationYear=2008`
                          year                    

  `available`             Filter by availability  `?available=true`

  `page`                  Zero-based page index   `?page=0`

  `size`                  Page size               `?size=10`

  `sort`                  Sort property and       `?sort=title,asc`
                          direction               
  -------------------------------------------------------------------------

Example combined request:

``` http
GET /api/books?title=java&author=martin&available=true&page=0&size=10&sort=title,asc
```

## 🧠 Design Decisions

### 1. Soft Delete Instead of Physical Deletion

Books are not physically removed from the database. Deleting a book
marks it as deleted using a `deleted` flag.

**Why?** - Preserve data and borrowing-related records. - Avoid
irreversible deletion. - Exclude deleted books from normal retrieval and
search operations. - Prevent deletion while a book is currently
borrowed.

This approach keeps historical data while ensuring deleted books are no
longer part of the active catalog.

### 2. Optimistic Locking with `@Version`

The `Book` entity uses JPA's `@Version` field to detect concurrent modifications. When two transactions attempt to update the same book based on an outdated version, Hibernate detects the version mismatch and prevents a silent overwrite.

**Why?**
- Helps protect data integrity when concurrent requests update the same book.
- Detects stale updates instead of silently overwriting another transaction's changes.
- Provides a lightweight concurrency-control mechanism without locking rows for the entire transaction duration.

This is particularly relevant to operations such as borrowing a book, where concurrent requests must not silently overwrite availability changes. An optimistic-lock conflict can cause a transaction to fail and roll back, rather than letting both changes proceed as if they were based on the latest state.

### 3. Dynamic Filtering with Specifications and Pagination

Book search uses Spring Data JPA Specifications to compose optional
filters dynamically, alongside pagination and sorting.

**Why?** - Filters can be used independently or combined. - Avoid
creating a separate repository method for every filter combination. -
Pagination helps handle larger catalogs efficiently. - Sorting is
configurable by the API consumer.

This keeps search logic flexible without making the repository interface
unnecessarily complex.

## 🔮 Future Improvements

Given more time, I would consider the following improvements:

-   🔐 Add authentication and role-based authorization for librarians
    and users.
-   🧾 Introduce a dedicated, consistent API error-response format with
    validation details.
-   🧪 Expand API/controller tests and add end-to-end tests for complete
    workflows.
-   🐳 Add Docker and Docker Compose for reproducible local setup.
-   📚 Add OpenAPI/Swagger documentation and example requests.
-   ⚙️ Improve database migration management with Flyway or Liquibase.
-   🔍 Review indexes and query performance as the catalog grows.
-   🧵 Strengthen concurrent borrowing protection and evaluate
    optimistic-lock conflict handling.
-   📦 Add CI pipeline to automatically build and run tests on pull
    requests.

## 🧱 Architecture at a Glance

``` mermaid
flowchart TD
    Client[API Client] --> Controller[Controllers]
    Controller --> Service[Services / Business Rules]
    Service --> Repository[Repositories]
    Repository --> DB[(PostgreSQL)]
    Controller --> DTO[Request / Response DTOs]
    Service --> Mapper[MapStruct Mappers]
    Mapper --> DTO
```

## 📌 Notes

-   ISBN is immutable through the update API.
-   A book can only be borrowed when available.
-   A book can only be returned when an active borrowing record exists.
-   Borrowing and returning update the book's availability and the
    borrowing record.
-   Deleted books are excluded from active catalog queries.
-   API pagination uses zero-based page numbering.

------------------------------------------------------------------------

### Built with ☕ and Spring Boot