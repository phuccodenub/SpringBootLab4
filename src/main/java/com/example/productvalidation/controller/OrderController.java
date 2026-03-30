package com.example.productvalidation.controller;

import com.example.productvalidation.model.Order;
import com.example.productvalidation.service.CartService;
import com.example.productvalidation.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @PostMapping("/checkout")
    public String checkout(Principal principal, RedirectAttributes redirectAttributes) {
        if (cartService.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi đặt hàng.");
            return "redirect:/cart";
        }

        Order order = orderService.createOrder(
                principal.getName(),
                cartService.getItems(),
                cartService.getTotal()
        );

        cartService.clear();
        redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công! Mã đơn hàng: #" + order.getId());
        return "redirect:/orders/" + order.getId();
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Principal principal, Model model) {
        Order order = orderService.getOrderById(id);
        if (order == null || !order.getUsername().equals(principal.getName())) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        return "order/detail";
    }

    @GetMapping
    public String orderHistory(Principal principal, Model model) {
        model.addAttribute("orders", orderService.getOrdersByUsername(principal.getName()));
        return "order/history";
    }
}
