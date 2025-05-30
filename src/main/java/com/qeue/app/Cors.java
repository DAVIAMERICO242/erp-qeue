package com.qeue.app;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Cors {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")              // Aplica em todos os endpoints
                        .allowedOrigins("*")            // Permite qualquer origem
                        .allowedMethods("*")            // Permite qualquer método (GET, POST, etc)
                        .allowedHeaders("*");           // Permite qualquer header
            }
        };
    }
}