package com.consartist.spring.cache;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@SpringBootApplication
@EnableCaching
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
class Book {
	@Id @NonNull
	private String isbn;
	@With
	private String title;
	private String author;
}

interface BookRepository extends ListCrudRepository<Book, Long> {
	Book findByIsbn(String isbn);
}

@RestController
@Slf4j
class BookRestController{
	private final BookRepository bookRepository;

	@Autowired
	public BookRestController(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	@RequestMapping("/books")
	public Collection<Book> books(){
		return bookRepository.findAll();
	}

	@RequestMapping("/books/{isbn}")
	@Cacheable(cacheNames = "books")
	public Book bookByIsbn(@PathVariable("isbn") String isbn){
		log.info("CACHE MISS");
		return bookRepository.findByIsbn(isbn);
	}

	@RequestMapping("/books/{isbn}/badUpdateTitle/{title}")
	public void badUpdateTitle(@PathVariable("isbn") String isbn, @PathVariable("title") String title){
		log.info("Bad Update Title");
		bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
	}

	@RequestMapping("/books/{isbn}/betterUpdateTitle/{title}")
	@CacheEvict(cacheNames = "books", key = "#isbn")
	public void betterUpdateTitle(@PathVariable("isbn") String isbn, @PathVariable("title") String title){
		log.info("Better Update Title");
		bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
	}

	@RequestMapping("/books/{isbn}/bestUpdateTitle/{title}")
	@CachePut(cacheNames = "books", key = "#isbn")
	public Book bestUpdateTitle(@PathVariable("isbn") String isbn, @PathVariable("title") String title){
		log.info("Best Update Title");
		return bookRepository.save(bookRepository.findByIsbn(isbn).withTitle(title));
	}
}