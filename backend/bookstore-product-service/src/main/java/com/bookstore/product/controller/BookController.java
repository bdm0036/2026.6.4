package com.bookstore.product.controller;

import com.bookstore.common.dto.PageDTO;
import com.bookstore.common.entity.Result;
import com.bookstore.product.entity.Book;
import com.bookstore.product.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/books")
    public Result<PageDTO<Book>> listBooks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        PageDTO<Book> result = bookService.listBooks(page, size, keyword, categoryId);
        return Result.success(result);
    }

    @GetMapping("/books/{id}")
    public Result<Book> getBook(@PathVariable Long id) {
        Book book = bookService.getById(id);
        return Result.success(book);
    }

    @GetMapping("/books/hot")
    public Result<List<Book>> getHotBooks(@RequestParam(defaultValue = "8") Integer limit) {
        List<Book> books = bookService.getHotBooks(limit);
        return Result.success(books);
    }

    @PostMapping("/books")
    public Result<Void> addBook(@RequestBody Book book) {
        bookService.addBook(book);
        return Result.success("添加图书成功");
    }

    @PutMapping("/books/{id}")
    public Result<Void> updateBook(@PathVariable Long id, @RequestBody Book book) {
        bookService.updateBook(id, book);
        return Result.success("更新图书成功");
    }

    @DeleteMapping("/books/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success("下架图书成功");
    }

    @GetMapping("/books/statistics")
    public Result<java.util.Map<String, Object>> getStatistics() {
        return Result.success(bookService.getStatistics());
    }
}
