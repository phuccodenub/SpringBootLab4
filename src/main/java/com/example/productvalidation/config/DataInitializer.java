package com.example.productvalidation.config;

import com.example.productvalidation.model.Account;
import com.example.productvalidation.model.Category;
import com.example.productvalidation.model.Role;
import com.example.productvalidation.repository.AccountRepository;
import com.example.productvalidation.repository.CategoryRepository;
import com.example.productvalidation.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(CategoryRepository categoryRepository,
                                      RoleRepository roleRepository,
                                      AccountRepository accountRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (categoryRepository.count() == 0) {
                Category c1 = new Category();
                c1.setName("Điện thoại");
                categoryRepository.save(c1);
                Category c2 = new Category();
                c2.setName("Laptop");
                categoryRepository.save(c2);
            }

            if (roleRepository.count() == 0) {
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                roleRepository.save(adminRole);

                Role userRole = new Role();
                userRole.setName("USER");
                roleRepository.save(userRole);
            }

            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            Role userRole = roleRepository.findByName("USER").orElseThrow();

            if (accountRepository.findByLoginName("admin").isEmpty()) {
                Account admin = new Account();
                admin.setLoginName("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRoles(Set.of(adminRole));
                accountRepository.save(admin);
            }

            if (accountRepository.findByLoginName("user1").isEmpty()) {
                Account user = new Account();
                user.setLoginName("user1");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRoles(Set.of(userRole));
                accountRepository.save(user);
            }
        };
    }
}
