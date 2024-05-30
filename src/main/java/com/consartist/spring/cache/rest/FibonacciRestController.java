package com.consartist.spring.cache.rest;

import com.consartist.spring.cache.data.FibonacciResult;
import io.swagger.v3.oas.annotations.Operation;
import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fibonacci")
@Slf4j
public class FibonacciRestController {
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
