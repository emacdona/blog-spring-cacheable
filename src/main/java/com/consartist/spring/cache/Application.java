package com.consartist.spring.cache;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableCaching
@EnableLoadTimeWeaving
@OpenAPIDefinition(
    servers = {
        @Server(url = "http://localhost:8080/", description = "Default Server URL."),
        @Server(url = "http://host:8080/", description = "My strange dev setup URL.")
    }
)
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

interface BookRepository extends JpaRepository<Book, Long>, ListCrudRepository<Book, Long> {
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
@RequestMapping("/books")
@Slf4j
class BookRestController {
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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class FibonacciResult implements Serializable {
  private BigInteger result;
  @Builder.Default
  @With
  private Integer callCount = 0;
  @Builder.Default
  @With
  private Boolean cached = false;
  @Builder.Default
  @With
  private String host = null;
}

@RestController
@RequestMapping("/fibonacci")
@Slf4j
class FibonacciRestController {
  // We need an injected reference so that Spring generated proxies run (because Spring generated
  // proxies are how Caching -- and a slew of other Spring features -- are implemented.
  @Autowired
  @Lazy
  private FibonacciRestController self;

  private static final ThreadLocal<Integer> callCount = new ThreadLocal<>();

  private final BiFunction<Function<BigInteger, BigInteger>, BigInteger, BigInteger>
      fibonacciCommon = (g, n) -> {
        if (n.compareTo(BigInteger.ONE) < 0) {
          return BigInteger.ZERO;
        } else if (n.equals(BigInteger.ONE)) {
          return BigInteger.ONE;
        } else if (n.equals(BigInteger.TWO)) {
          return BigInteger.ONE;
        } else {
          return g.apply(n.subtract(BigInteger.ONE)).add(g.apply(n.subtract(BigInteger.TWO)));
        }
      };

  public BigInteger slowFibonacci(@PathVariable("n") BigInteger n) {
    callCount.set(callCount.get() + 1);
    return fibonacciCommon.apply(self::slowFibonacci, n);
  }

  @Cacheable("fibonacciNumbers")
  public BigInteger fastFibonacci(@PathVariable("n") BigInteger n) {
    callCount.set(callCount.get() + 1);
    return fibonacciCommon.apply(self::fastFibonacci, n);
  }

  @Operation(summary = "Find the nth Fibonacci number.",
      description = "Finds this nth Fibonacci number without caching intermediate results. Slow!")
  @GetMapping("/slow/{n}")
  public FibonacciResult slowFibonacciEndpoint(@PathVariable("n") BigInteger n) {
    callCount.set(0);
    return FibonacciResult.builder()
        .result(fibonacciCommon.apply(self::slowFibonacci, n))
        .callCount(callCount.get())
        .build();
  }

  @Operation(summary = "Find the nth Fibonacci number.",
      description = "Finds this nth Fibonacci number -- and caches intermediate results. Fast!")
  @GetMapping("/fast/{n}")
  @Cacheable("fibonacciPayloads")
  public FibonacciResult fastFibonacciEndpoint(@PathVariable("n") BigInteger n) {
    callCount.set(0);
    return FibonacciResult.builder()
        .result(fibonacciCommon.apply(self::fastFibonacci, n))
        .callCount(callCount.get())
        .build();
  }

  @Operation(summary = "Clear all Fibonacci caches.",
      description = "Clears all Fibonacci caches.")
  @GetMapping("/clear")
  @CacheEvict(cacheNames = {"fibonacciNumbers", "fibonacciPayloads"}, allEntries = true)
  public void clearCache() {
  }
}