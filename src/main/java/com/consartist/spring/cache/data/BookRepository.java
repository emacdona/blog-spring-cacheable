package com.consartist.spring.cache.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;

public interface BookRepository extends JpaRepository<Book, Long>, ListCrudRepository<Book, Long> {
  Book findByIsbn(String isbn);
}
