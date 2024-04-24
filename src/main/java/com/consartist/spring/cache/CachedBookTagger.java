package com.consartist.spring.cache;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import static org.apache.commons.lang3.reflect.MethodUtils.invokeMethod;

@Aspect
@Slf4j
public class CachedBookTagger {
    // this pointcut... and not the @Before one... necessitated this:
    // --add-opens java.base/java.lang=ALL-UNNAMED.
    @Around(value = "execution(void org.springframework.cache.Cache+.put(Object+, Object+)) && args(key, value)", argNames = "pjp,key,value")
    public void tagCachedBook(ProceedingJoinPoint pjp, Object key, Object value) throws Throwable {
        // If you're using dev tools, value will have been loaded by the RefreshClassLoader.
        // Book.class will be loaded by the AppClassLoader.
        Class<?> bookClassFromValueClassLoader = value.getClass().getClassLoader().loadClass(Book.class.getName());

        if(bookClassFromValueClassLoader.isInstance(value)) {
            pjp.proceed(new Object[]{key, invokeMethod(value, "withCached", true)});
        }
        else{
            pjp.proceed(new Object[]{key, value});
        }
    }
}
