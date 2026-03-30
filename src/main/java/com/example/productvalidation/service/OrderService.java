package com.example.productvalidation.service;

import com.example.productvalidation.dto.CartItem;
import com.example.productvalidation.model.Order;
import com.example.productvalidation.model.OrderDetail;
import com.example.productvalidation.model.Product;
import com.example.productvalidation.repository.OrderRepository;
import com.example.productvalidation.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order createOrder(String username, Collection<CartItem> cartItems, double totalAmount) {
        Order order = new Order();
        order.setUsername(username);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus("COMPLETED");

        for (CartItem cartItem : cartItems) {
            OrderDetail detail = new OrderDetail();
            Product product = productRepository.findById(cartItem.getProductId()).orElse(null);
            detail.setProduct(product);
            detail.setProductName(cartItem.getProductName());
            detail.setQuantity(cartItem.getQuantity());
            detail.setPrice(cartItem.getPrice());
            detail.setSubtotal(cartItem.getSubtotal());
            order.addOrderDetail(detail);
        }

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findByUsernameOrderByOrderDateDesc(username);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }
}
