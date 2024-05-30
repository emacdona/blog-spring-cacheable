package com.consartist.spring.cache.data;

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
public class FibonacciResult implements Serializable {
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
