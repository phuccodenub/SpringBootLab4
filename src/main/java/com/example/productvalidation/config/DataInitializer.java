package com.example.productvalidation.config;

import com.example.productvalidation.model.Category;
import com.example.productvalidation.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                Category c1 = new Category();
                c1.setName("Điện thoại");
                categoryRepository.save(c1);
                Category c2 = new Category();
                c2.setName("Laptop");
                categoryRepository.save(c2);
            }
        };
    }
}
