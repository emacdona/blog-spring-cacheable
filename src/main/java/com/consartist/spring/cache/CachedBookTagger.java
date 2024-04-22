package com.consartist.spring.cache;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
@Slf4j
public class CachedBookTagger {
    /*
    // Didn't work. I'm guessing b/c Cacheable annotations caches, then returns value from cache (ie: cached was ALWAYS true, even on the first get)
    @Before(value = "execution(void org.springframework.cache.Cache+.put(Object+, Object+)) && args(key, value)", argNames = "jp,key,value")
    public void tagCachedBook(JoinPoint jp, Object key, Object value) {
        log.info("{}: \n\t<{}, {}>\n\t<{}, {}>",
                value,
                value.getClass().getClassLoader(),
                value.getClass().getName(),
                Book.class.getClassLoader(),
                Book.class.getName());
        if(value instanceof Book){
            log.info("Setting cached.");
            ((Book) value).setCached(true);
        }
        else{
            log.info("Not setting cached.");
        }
    }
    */

    // this pointcut... and not the @Before one... necessitated this:
    // --add-opens java.base/java.lang=ALL-UNNAMED
    @Around(value = "execution(void org.springframework.cache.Cache+.put(Object+, Object+)) && args(key, value)", argNames = "pjp,key,value")
    public void tagCachedBook(ProceedingJoinPoint pjp, Object key, Object value) throws Throwable {
        log.info("{}: \n\t<{}, {}>\n\t<{}, {}>",
                value,
                value.getClass().getClassLoader(),
                value.getClass().getName(),
                Book.class.getClassLoader(),
                Book.class.getName());

        if(value instanceof Book){
            pjp.proceed(new Object[]{key, ((Book) value).withCached(true)});
        }
        else{
            pjp.proceed(new Object[]{key, value});
        }
    }
}
