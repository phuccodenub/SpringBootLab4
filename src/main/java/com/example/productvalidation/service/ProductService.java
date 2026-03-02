package com.example.productvalidation.service;

import com.example.productvalidation.model.CategoryOption;
import com.example.productvalidation.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {
    private static final List<CategoryOption> CATEGORIES = List.of(
            new CategoryOption("phone", "Điện thoại"),
            new CategoryOption("laptop", "Laptop")
    );

    private final Map<Long, Product> products = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public List<Product> findAll() {
        synchronized (products) {
            return products.values().stream().map(this::copy).toList();
        }
    }

    public Optional<Product> findById(Long id) {
        synchronized (products) {
            var product = products.get(id);
            return Optional.ofNullable(product == null ? null : copy(product));
        }
    }

    public Product create(Product product) {
        synchronized (products) {
            var id = sequence.getAndIncrement();
            var toSave = copy(product);
            toSave.setId(id);
            products.put(id, toSave);
            return copy(toSave);
        }
    }

    public boolean update(Long id, Product product) {
        synchronized (products) {
            if (!products.containsKey(id)) {
                return false;
            }

            var toSave = copy(product);
            toSave.setId(id);
            products.put(id, toSave);
            return true;
        }
    }

    public Optional<Product> delete(Long id) {
        synchronized (products) {
            var removed = products.remove(id);
            return Optional.ofNullable(removed == null ? null : copy(removed));
        }
    }

    public List<CategoryOption> getCategories() {
        return new ArrayList<>(CATEGORIES);
    }

    public Optional<CategoryOption> findCategoryByKey(String key) {
        return CATEGORIES.stream()
                .filter(category -> category.key().equalsIgnoreCase(key))
                .findFirst();
    }

    private Product copy(Product source) {
        return new Product(
                source.getId(),
                source.getName(),
                source.getPrice(),
                source.getImageFileName(),
                source.getCategoryKey(),
                source.getCategoryLabel()
        );
    }
}
