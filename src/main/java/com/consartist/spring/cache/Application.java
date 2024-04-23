package com.consartist.spring.cache;

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
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@SpringBootApplication
@EnableCaching
@EnableLoadTimeWeaving
public class Application {
	public static void main(String[] args) {
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(Application.class, args);
	}
}

interface BookRepository extends ListCrudRepository<Book, Long> {
	Book findByIsbn(String isbn);
}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
class Book {
	@Id
	@NonNull
	private String isbn;
	@With
	private String title;
	private String author;
	@Transient
	@With
	private Boolean cached = false;
}

@RestController
@Slf4j
class BookRestController{
	private final BookRepository bookRepository;

	@Autowired
	public BookRestController(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	@GetMapping("/books")
	public Collection<Book> books(){
		return bookRepository.findAll();
	}

	@GetMapping("/books/{isbn}")
	@Cacheable(cacheNames = "books")
	public Book bookByIsbn(@PathVariable("isbn") String isbn){
		log.info("CACHE MISS");
		return bookRepository.findByIsbn(isbn);
	}

	@GetMapping("/books/{isbn}/badUpdateTitle/{title}")
	public Book badUpdateTitle(@PathVariable("isbn") String isbn, @PathVariable("title") String title){
		log.info("Bad Update Title");
		return bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
	}

	@GetMapping("/books/{isbn}/betterUpdateTitle/{title}")
	@CacheEvict(cacheNames = "books", key = "#isbn")
	public Book betterUpdateTitle(@PathVariable("isbn") String isbn, @PathVariable("title") String title){
		log.info("Better Update Title");
		return bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
	}

	@GetMapping("/books/{isbn}/bestUpdateTitle/{title}")
	@CachePut(cacheNames = "books", key = "#isbn")
	public Book bestUpdateTitle(@PathVariable("isbn") String isbn, @PathVariable("title") String title){
		log.info("Best Update Title");
		return bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
	}
}