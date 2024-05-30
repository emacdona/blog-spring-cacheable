package com.consartist.spring.cache;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.net.InetAddress;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.reflect.MethodUtils.invokeMethod;

@Aspect
@Slf4j
public class BookAdvice {
   @Pointcut(value = "execution(void org.springframework.cache.Cache+.put(Object+, Object+))")
   public void cachePut() {
   }

   @Pointcut(value = "execution(Book com.consartist.spring.cache.BookRestController.*(..))")
   public void bookReturned() {
   }

   @Pointcut(value = "execution(java.util.Collection<Book> com.consartist.spring.cache.BookRestController.*(..))")
   public void booksReturned() {
   }

   @Pointcut(value = "execution(Object org.springframework.cache.interceptor.CacheInterceptor.invoke(org.aopalliance.intercept.MethodInvocation))")
   public void cacheInterceptorInvoke() {
   }

   // this pointcut... and not the @Before one... necessitated this:
   // --add-opens java.base/java.lang=ALL-UNNAMED.
   //@Around(value = "execution(void org.springframework.cache.Cache+.put(Object+, Object+)) && args(key, value)", argNames = "pjp,key,value")
   @Around(value = "cachePut() && args(key, value)", argNames = "pjp,key,value")
   public void tagCachedBook(ProceedingJoinPoint pjp, Object key, Object value) throws Throwable {
      // Flag cached records as having been cached. Don't cache host names.
      pjp.proceed(new Object[]{key,
            BookTagger.of(value)
                  .tag("withCached", true, Boolean.class)
                  .tag("withHost", null, String.class)
                  .get()
      });
   }

   @Around(value = "bookReturned() || cacheInterceptorInvoke()", argNames = "pjp")
   public Object tagReturnedBookWithHost(ProceedingJoinPoint pjp) throws Throwable {
      return BookTagger.of(pjp.proceed())
            .tag("withHost", InetAddress.getLocalHost().getHostName(), String.class)
            .get();
   }

   @Around(value = "booksReturned()", argNames = "pjp")
   public Object tagReturnedBooksWithHost(ProceedingJoinPoint pjp) throws Throwable {
      return ((Collection) pjp.proceed()).stream()
            .map(
                  new Function() {
                     @Override
                     @SneakyThrows
                     public Object apply(Object o) {
                        return BookTagger.of(o)
                              .tag("withHost", InetAddress.getLocalHost().getHostName(), String.class)
                              .get();
                     }
                  }
            )
            .collect(Collectors.toList());

   }

   public static class BookTagger {
      private Object maybeBook;

      private BookTagger(Object maybeBook) {
         this.maybeBook = maybeBook;
      }

      public static BookTagger of(Object maybeBook) {
         return new BookTagger(maybeBook);
      }

      public Object get() {
         return maybeBook;
      }

      @SneakyThrows
      public BookTagger tag(@NonNull String tagMethodName, Object tagValue, @NonNull Class<?> tagValueClass) {
         maybeBook = maybeBook == null ?
               null :
               // https://stackoverflow.com/questions/1921238/getclass-getclassloader-is-null-why
               (maybeBook.getClass().getClassLoader() != null &&

               // If you're using dev tools, value will have been loaded by the RefreshClassLoader.
               // Book.class will be loaded by the AppClassLoader.
               maybeBook.getClass().getClassLoader().loadClass(Book.class.getName()).isInstance(maybeBook)) ?
                     tagValue != null ?
                           invokeMethod(maybeBook, tagMethodName, tagValue) :
                           invokeMethod(maybeBook, tagMethodName, new Object[]{null}, new Class<?>[]{tagValueClass})
                     : maybeBook;
         return this;
      }
   }
}
