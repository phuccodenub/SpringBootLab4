package com.example.productvalidation.dto;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private String image;
    private String categoryName;

    public Double getSubtotal() {
        return (price != null && quantity != null) ? price * quantity : 0.0;
    }
}
