package com.consartist.spring.cache.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FibonacciResult implements Serializable, CacheMeta {
  private BigInteger result;
  @Builder.Default
  @With
  @Schema(description = "The number of intermediate, recursive calls made to obtain this result.")
  private Integer callCount = 0;
  @Builder.Default
  @With
  private Boolean cached = false;
  @Builder.Default
  @With
  private String host = null;
}
