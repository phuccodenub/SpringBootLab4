package com.example.productvalidation.controller;

import com.example.productvalidation.dto.ProductForm;
import com.example.productvalidation.model.CategoryOption;
import com.example.productvalidation.model.Product;
import com.example.productvalidation.service.FileStorageService;
import com.example.productvalidation.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping
public class ProductController {
    private final ProductService productService;
    private final FileStorageService fileStorageService;

    public ProductController(ProductService productService, FileStorageService fileStorageService) {
        this.productService = productService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping({"", "/"})
    public String home() {
        return "redirect:/products";
    }

    @GetMapping("/products")
    public String index(
            @RequestParam(name = "q", required = false) String query,
            Model model
    ) {
        model.addAttribute("products", filterProducts(query));
        model.addAttribute("query", query == null ? "" : query);
        return "products/index";
    }

    @GetMapping("/products/create")
    public String createPage(Model model) {
        return renderCreate(model, new ProductForm());
    }

    @PostMapping("/products")
    public String create(
            @Valid @ModelAttribute("productForm") ProductForm productForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validateImageForCreate(productForm, bindingResult);
        var category = resolveCategory(productForm.getCategoryKey(), bindingResult);

        if (bindingResult.hasErrors() || category == null) {
            return renderCreate(model, productForm);
        }

        var imageFileName = fileStorageService.save(productForm.getImageFile());
        var product = mapToProduct(productForm, category, imageFileName);
        productService.create(product);

        redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công.");
        return "redirect:/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var existingProduct = productService.findById(id);
        if (existingProduct.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
            return "redirect:/products";
        }

        var product = existingProduct.get();
        var form = new ProductForm();
        form.setName(product.getName());
        form.setPrice(product.getPrice());
        form.setCategoryKey(product.getCategoryKey());
        form.setExistingImageFileName(product.getImageFileName());

        model.addAttribute("productId", id);
        model.addAttribute("productForm", form);
        model.addAttribute("categories", productService.getCategories());
        return "products/edit";
    }

    @PostMapping("/products/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("productForm") ProductForm productForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        var existingProduct = productService.findById(id);
        if (existingProduct.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
            return "redirect:/products";
        }

        validateImageForUpdate(productForm, bindingResult);
        var category = resolveCategory(productForm.getCategoryKey(), bindingResult);

        if (bindingResult.hasErrors() || category == null) {
            model.addAttribute("productId", id);
            model.addAttribute("categories", productService.getCategories());
            return "products/edit";
        }

        var product = existingProduct.get();
        var imageFileName = product.getImageFileName();
        if (productForm.hasNewImage()) {
            imageFileName = fileStorageService.save(productForm.getImageFile());
            fileStorageService.deleteIfExists(product.getImageFileName());
        }

        var updatedProduct = mapToProduct(productForm, category, imageFileName);
        productService.update(id, updatedProduct);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công.");
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var deletedProduct = productService.delete(id);
        if (deletedProduct.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
            return "redirect:/products";
        }

        fileStorageService.deleteIfExists(deletedProduct.get().getImageFileName());
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công.");
        return "redirect:/products";
    }

    private String renderCreate(Model model, ProductForm productForm) {
        model.addAttribute("productForm", productForm);
        model.addAttribute("categories", productService.getCategories());
        return "products/create";
    }

    private List<Product> filterProducts(String query) {
        var products = productService.findAll();
        if (query == null || query.isBlank()) {
            return products;
        }

        var normalizedQuery = query.toLowerCase(Locale.ROOT).trim();
        return products.stream()
                .filter(product ->
                        product.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                                || product.getCategoryLabel().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .toList();
    }

    private CategoryOption resolveCategory(String categoryKey, BindingResult bindingResult) {
        if (categoryKey == null || categoryKey.isBlank()) {
            return null;
        }

        var category = productService.findCategoryByKey(categoryKey);
        if (category.isEmpty()) {
            bindingResult.rejectValue("categoryKey", "invalid", "Danh mục không hợp lệ");
            return null;
        }

        return category.get();
    }

    private void validateImageForCreate(ProductForm productForm, BindingResult bindingResult) {
        if (!productForm.hasNewImage()) {
            bindingResult.rejectValue("imageFile", "required", "Tên hình ảnh không được để trống");
            return;
        }

        validateImageNameLength(productForm, bindingResult);
    }

    private void validateImageForUpdate(ProductForm productForm, BindingResult bindingResult) {
        if (productForm.hasNewImage()) {
            validateImageNameLength(productForm, bindingResult);
        }
    }

    private void validateImageNameLength(ProductForm productForm, BindingResult bindingResult) {
        var originalFileName = productForm.getImageFile().getOriginalFilename();
        if (originalFileName != null && originalFileName.length() > 200) {
            bindingResult.rejectValue("imageFile", "length", "Tên hình ảnh không quá 200 kí tự");
        }
    }

    private Product mapToProduct(ProductForm productForm, CategoryOption category, String imageFileName) {
        var product = new Product();
        product.setName(productForm.getName().trim());
        product.setPrice(productForm.getPrice());
        product.setImageFileName(imageFileName);
        product.setCategoryKey(category.key());
        product.setCategoryLabel(category.label());
        return product;
    }
}
