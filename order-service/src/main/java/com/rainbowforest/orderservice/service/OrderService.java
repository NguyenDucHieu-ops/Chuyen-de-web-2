package com.rainbowforest.orderservice.service;

import com.rainbowforest.orderservice.domain.Order;
import java.util.List;

public interface OrderService {
    public Order saveOrder(Order order);

    public List<Order> getAllOrders(); // Thêm hàm này

    public List<Order> getOrdersByUserId(Long userId); // Thêm hàm này

    public Order getOrderById(Long id);
}