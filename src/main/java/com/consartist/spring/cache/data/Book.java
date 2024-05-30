package com.consartist.spring.cache.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.With;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book implements Serializable {
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
