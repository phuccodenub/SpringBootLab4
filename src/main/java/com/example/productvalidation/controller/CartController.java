package com.example.productvalidation.controller;

import com.example.productvalidation.dto.CartItem;
import com.example.productvalidation.model.Product;
import com.example.productvalidation.service.CartService;
import com.example.productvalidation.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("cartTotal", cartService.getTotal());
        return "cart/index";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại.");
            return "redirect:/products";
        }
        if (quantity < 1) {
            quantity = 1;
        }

        CartItem item = new CartItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setImage(product.getImage());
        item.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : "");

        cartService.addItem(item);
        redirectAttributes.addFlashAttribute("success", "Đã thêm \"" + product.getName() + "\" vào giỏ hàng.");
        return "redirect:/products";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId,
                                  @RequestParam int quantity,
                                  RedirectAttributes redirectAttributes) {
        cartService.updateQuantity(productId, quantity);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật giỏ hàng.");
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 RedirectAttributes redirectAttributes) {
        cartService.removeItem(productId);
        redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        cartService.clear();
        redirectAttributes.addFlashAttribute("success", "Đã xóa toàn bộ giỏ hàng.");
        return "redirect:/cart";
    }
}
