package com.example.library.mapper;

import com.example.library.dto.response.BorrowingResponse;
import com.example.library.entity.Borrowing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BorrowingMapper {

    @Mapping(source = "book.id", target = "bookId")
    BorrowingResponse toResponse(Borrowing borrowing);
}