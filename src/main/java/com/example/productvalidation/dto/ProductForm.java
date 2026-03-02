package com.example.productvalidation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class ProductForm {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm không quá 200 kí tự")
    private String name;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "1", message = "Giá sản phẩm chỉ nhập từ 1 đến 9999999")
    @DecimalMax(value = "9999999", message = "Giá sản phẩm chỉ nhập từ 1 đến 9999999")
    private BigDecimal price;

    @NotBlank(message = "Vui lòng chọn danh mục")
    private String categoryKey;

    private MultipartFile imageFile;
    private String existingImageFileName;

    public boolean hasNewImage() {
        return imageFile != null && !imageFile.isEmpty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }

    public String getExistingImageFileName() {
        return existingImageFileName;
    }

    public void setExistingImageFileName(String existingImageFileName) {
        this.existingImageFileName = existingImageFileName;
    }
}
