package com.example.productvalidation.config;

import com.example.productvalidation.model.Account;
import com.example.productvalidation.model.Category;
import com.example.productvalidation.model.Product;
import com.example.productvalidation.model.Role;
import com.example.productvalidation.repository.AccountRepository;
import com.example.productvalidation.repository.CategoryRepository;
import com.example.productvalidation.repository.ProductRepository;
import com.example.productvalidation.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(CategoryRepository categoryRepository,
                                      RoleRepository roleRepository,
                                      AccountRepository accountRepository,
                                      ProductRepository productRepository,
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

                try {
                    if (productRepository.count() < 6) {
                        System.out.println("Seeding sample products for lab demo (pagination)...");
                        Category phone = categoryRepository.findByName("Điện thoại").orElse(null);
                        Category laptop = categoryRepository.findByName("Laptop").orElse(null);
                        if (phone != null && laptop != null) {
                            Set<String> existing = productRepository.findAll().stream()
                                    .map(Product::getName)
                                    .collect(Collectors.toSet());
                            Object[][] rows = {
                                    {"iPhone 15", 22_990_000d, phone},
                                    {"Samsung Galaxy S24", 18_500_000d, phone},
                                    {"Xiaomi 14", 12_900_000d, phone},
                                    {"MacBook Air M3", 28_990_000d, laptop},
                                    {"Dell XPS 15", 35_000_000d, laptop},
                                    {"ASUS Zenbook", 19_490_000d, laptop},
                            };
                            for (Object[] row : rows) {
                                String name = (String) row[0];
                                if (!existing.contains(name)) {
                                    Product p = new Product();
                                    p.setName(name);
                                    p.setPrice((Double) row[1]);
                                    p.setCategory((Category) row[2]);
                                    productRepository.save(p);
                                    existing.add(name);
                                }
                            }
                            System.out.println("✓ Sample products ready");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠ Skipping product seed: " + e.getClass().getSimpleName());
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
