package com.consartist.spring.cache;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.With;

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
