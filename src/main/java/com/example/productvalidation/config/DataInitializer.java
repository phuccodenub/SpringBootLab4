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
            System.out.println("===== Starting Data Initialization =====");
            try {
                // Initialize Categories
                try {
                    if (categoryRepository.count() == 0) {
                        System.out.println("Creating categories...");
                        Category c1 = new Category();
                        c1.setName("Điện thoại");
                        categoryRepository.save(c1);
                        Category c2 = new Category();
                        c2.setName("Laptop");
                        categoryRepository.save(c2);
                        System.out.println("✓ Categories initialized");
                    }
                } catch (Exception e) {
                    System.out.println("⚠ Skipping categories: " + e.getClass().getSimpleName());
                }

                // Initialize Roles and Accounts  
                try {
                    if (roleRepository.count() == 0) {
                        System.out.println("Creating roles...");
                        Role adminRole = new Role();
                        adminRole.setName("ADMIN");
                        roleRepository.save(adminRole);

                        Role userRole = new Role();
                        userRole.setName("USER");
                        roleRepository.save(userRole);
                        System.out.println("✓ Roles initialized");
                    }

                    Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
                    Role userRole = roleRepository.findByName("USER").orElseThrow();

                    if (accountRepository.findByLoginName("admin").isEmpty()) {
                        System.out.println("Creating admin account...");
                        Account admin = new Account();
                        admin.setLoginName("admin");
                        admin.setPassword(passwordEncoder.encode("admin123"));
                        admin.setRoles(Set.of(adminRole));
                        accountRepository.save(admin);
                        System.out.println("✓ Admin account created (admin/admin123)");
                    }

                    if (accountRepository.findByLoginName("user1").isEmpty()) {
                        System.out.println("Creating user1 account...");
                        Account user = new Account();
                        user.setLoginName("user1");
                        user.setPassword(passwordEncoder.encode("123456"));
                        user.setRoles(Set.of(userRole));
                        accountRepository.save(user);
                        System.out.println("✓ User account created (user1/123456)");
                    }
                } catch (Exception e) {
                    System.out.println("⚠ Skipping roles/accounts: " + e.getClass().getSimpleName());
                }
            } catch (Exception e) {
                System.out.println("⚠ Data initialization not available - running without database schema");
            }
            System.out.println("===== Data Initialization Complete =====\n");
        };
    }
}
