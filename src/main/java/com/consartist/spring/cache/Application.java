package com.consartist.spring.cache;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Collection;

@SpringBootApplication
@EnableCaching
@EnableLoadTimeWeaving
@OpenAPIDefinition(
    servers = {
       @Server(url = "http://localhost:8080/", description = "Default Server URL"),

       // A concession to my strange local development environment
       @Server(url = "http://host:8080/", description = "Default Server URL")
    }
)
public class Application {
   public static void main(String[] args) {
      SpringApplication.run(Application.class, args);
   }
}

interface BookRepository extends JpaRepository<Book,  Long>, ListCrudRepository<Book, Long> {
   Book findByIsbn(String isbn);
}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
class Book implements Serializable {
   @Id
   @NonNull
   private String isbn;
   @With
   private String title;
   private String author;
   @Transient
   @With
   private Boolean cached = false;
   @Transient
   @With
   private String host = null;
}

@RestController
@Slf4j
class BookRestController {
   private final BookRepository bookRepository;

   @Autowired
   public BookRestController(BookRepository bookRepository) {
      this.bookRepository = bookRepository;
   }

   @Operation(summary = "Clear 'books' cache.",
         description = "Removes all cached books from the cache.")
   @GetMapping("/books/clear")
   @CacheEvict(cacheNames = "books", allEntries = true)
   public void clearCache() {
   }

   @Operation(summary = "Retrieve all books.",
         description = "Retrieves all books. Does not cache result(s).")
   @GetMapping("/books")
   public Collection<Book> books() {
      return bookRepository.findAll();
   }

   @Operation(summary = "Retrieve a single book.",
         description = "Retrieve a single book, identified by ISBN. Cache the result.")
   @GetMapping("/books/{isbn}")
   @Cacheable(cacheNames = "books")
   public Book bookByIsbn(@PathVariable("isbn") String isbn) {
      return bookRepository.findByIsbn(isbn);
   }

   @Operation(summary = "BAD: Update book title. Do not update cache.",
         description = "BAD: Updates a single book's title, but does not add the updated book to the cache.")
   @GetMapping("/books/{isbn}/badUpdateTitle/{title}")
   public Book badUpdateTitle(@PathVariable("isbn") String isbn, @PathVariable("title") String title) {
      return bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
   }

   @Operation(summary = "Update book title. Evict any cached copy of this book.",
         description = "Updates a single book's title. Removes the book from the cache if present.")
   @GetMapping("/books/{isbn}/betterUpdateTitle/{title}")
   @CacheEvict(cacheNames = "books", key = "#isbn")
   public Book betterUpdateTitle(@PathVariable("isbn") String isbn, @PathVariable("title") String title) {
      return bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
   }

   @Operation(summary = "Update book title. Update cache with result",
         description = "Updates a single book's title. Updates the cache with the result.")
   @GetMapping("/books/{isbn}/bestUpdateTitle/{title}")
   @CachePut(cacheNames = "books", key = "#isbn")
   public Book bestUpdateTitle(@PathVariable("isbn") String isbn, @PathVariable("title") String title) {
      return bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
   }
}