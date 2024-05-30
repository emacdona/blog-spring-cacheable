package com.consartist.spring.cache;

import static org.apache.commons.lang3.reflect.MethodUtils.invokeMethod;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@Slf4j
public class Advice {
  @Pointcut(value = "execution(void org.springframework.cache.Cache+.put(Object+, Object+))")
  public void cachePut() {
  }

  @Pointcut(value = "execution(Book com.consartist.spring.cache.BookRestController.*(..))")
  public void bookReturned() {
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Pointcut(value = "execution(FibonacciResult com.consartist.spring.cache.FibonacciRestController.*(..))")
  public void fibonacciResultReturned() {
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Pointcut(value = "execution(java.util.Collection<Book> com.consartist.spring.cache.BookRestController.*(..))")
  public void booksReturned() {
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Pointcut(value = "execution(Object org.springframework.cache.interceptor.CacheInterceptor.invoke(org.aopalliance.intercept.MethodInvocation))")
  public void cacheInterceptorInvoke() {
  }

  // this pointcut... and not the @Before one... necessitated this:
  // --add-opens java.base/java.lang=ALL-UNNAMED.
  @Around(value = "cachePut() && args(key, value)", argNames = "pjp,key,value")
  public void tagCachedTaggable(ProceedingJoinPoint pjp, Object key, Object value)
      throws Throwable {
    // Flag cached records as having been cached. Don't cache host names.
    pjp.proceed(new Object[] {key,
        Tagger.of(value)
            .tag("withCached", true, Boolean.class)
            .tag("withHost", null, String.class)
            .tag("withCallCount", 0, Integer.class)
            .get()
    });
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Around(value = "fibonacciResultReturned() || bookReturned() || cacheInterceptorInvoke()", argNames = "pjp")
  public Object tagReturnedTaggableWithHost(ProceedingJoinPoint pjp) throws Throwable {
    return Tagger.of(pjp.proceed())
        .tag("withHost", InetAddress.getLocalHost().getHostName(), String.class)
        .get();
  }

  @Around(value = "booksReturned()", argNames = "pjp")
  @SuppressWarnings("unchecked")
  public Object tagReturnedBooksWithHost(ProceedingJoinPoint pjp) throws Throwable {
    return ((Collection) pjp.proceed()).stream()
        .map(
            new Function() {
              @Override
              @SneakyThrows
              public Object apply(Object o) {
                return Tagger.of(o)
                    .tag("withHost", InetAddress.getLocalHost().getHostName(), String.class)
                    .get();
              }
            }
        )
        .collect(Collectors.toList());

  }

  public static class Tagger {
    private Object maybeTaggable;

    private Tagger(Object maybeTaggable) {
      this.maybeTaggable = maybeTaggable;
    }

    public static Tagger of(Object maybeTaggable) {
      return new Tagger(maybeTaggable);
    }

    public Object get() {
      return maybeTaggable;
    }

    private static final List<String> taggableClassNames = Arrays.asList(
        Book.class.getName(),
        FibonacciResult.class.getName()
    );

    @SneakyThrows
    public Tagger tag(@NonNull String tagMethodName, Object tagValue,
                      @NonNull Class<?> tagValueClass) {
      if (maybeTaggable != null

          // https://stackoverflow.com/questions/1921238/getclass-getclassloader-is-null-why
          && maybeTaggable.getClass().getClassLoader() != null

          && taggableClassNames.stream()
          .anyMatch(
              new Predicate<String>() {
                @Override
                @SneakyThrows
                public boolean test(String className) {
                  // If you're using dev tools, value will have been loaded by the
                  // RefreshClassLoader. Book.class will be loaded by the AppClassLoader.
                  return maybeTaggable.getClass().getClassLoader()
                      .loadClass(className).isInstance(maybeTaggable);
                }
              }
          )
      ) {
        try {
          maybeTaggable = tagValue != null
              ? invokeMethod(maybeTaggable, tagMethodName, tagValue)
              : invokeMethod(maybeTaggable, tagMethodName, new Object[] {null},
                new Class<?>[] {tagValueClass});
        } catch (NoSuchMethodException ignore) {
          // No-op
        }
      }

      return this;
    }
  }
}
