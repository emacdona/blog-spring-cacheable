package com.consartist.spring.cache;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

@SpringBootApplication
@EnableCaching
@EnableLoadTimeWeaving
@OpenAPIDefinition(
    servers = {
        @Server(url = "http://localhost:8080/", description = "Default Server URL."),
        @Server(url = "http://host:8080/", description = "My strange dev setup URL.")
    }
)
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

