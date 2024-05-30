package com.consartist.spring.cache.rest;

import com.consartist.spring.cache.data.Book;
import com.consartist.spring.cache.data.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
@Slf4j
public class BookRestController {
  private final BookRepository bookRepository;

  @Autowired
  public BookRestController(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  @Operation(summary = "Clear 'books' cache.",
      description = "Removes all cached books from the cache.")
  @GetMapping("/clear")
  @CacheEvict(cacheNames = "books", allEntries = true)
  public void clearCache() {
  }

  @Operation(summary = "Retrieve all books.",
      description = "Retrieves all books. Does not cache result(s).")
  @GetMapping
  public Collection<Book> books() {
    return bookRepository.findAll();
  }

  @Operation(summary = "Retrieve a single book.",
      description = "Retrieve a single book, identified by ISBN. Cache the result.")
  @GetMapping("/{isbn}")
  @Cacheable(cacheNames = "books")
  public Book bookByIsbn(@PathVariable("isbn") String isbn) {
    return bookRepository.findByIsbn(isbn);
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Operation(summary = "BAD: Update book title. Do not update cache.",
      description = "BAD: Updates a single book's title, but does not add the updated book to the cache.")
  @GetMapping("/{isbn}/badUpdateTitle/{title}")
  public Book badUpdateTitle(@PathVariable("isbn") String isbn,
                             @PathVariable("title") String title) {
    return bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
  }

  @Operation(summary = "Update book title. Evict any cached copy of this book.",
      description = "Updates a single book's title. Removes the book from the cache if present.")
  @GetMapping("/{isbn}/betterUpdateTitle/{title}")
  @CacheEvict(cacheNames = "books", key = "#isbn")
  public Book betterUpdateTitle(@PathVariable("isbn") String isbn,
                                @PathVariable("title") String title) {
    return bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
  }

  @Operation(summary = "Update book title. Update cache with result",
      description = "Updates a single book's title. Updates the cache with the result.")
  @GetMapping("/{isbn}/bestUpdateTitle/{title}")
  @CachePut(cacheNames = "books", key = "#isbn")
  public Book bestUpdateTitle(@PathVariable("isbn") String isbn,
                              @PathVariable("title") String title) {
    return bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
  }
}
