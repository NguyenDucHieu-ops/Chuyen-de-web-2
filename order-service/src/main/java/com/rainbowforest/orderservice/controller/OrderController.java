package com.rainbowforest.orderservice.controller;

import com.rainbowforest.orderservice.domain.Item;
import com.rainbowforest.orderservice.domain.Order;
import com.rainbowforest.orderservice.domain.User;
import com.rainbowforest.orderservice.domain.Product;
import com.rainbowforest.orderservice.feignclient.UserClient;
import com.rainbowforest.orderservice.feignclient.ProductClient;
import com.rainbowforest.orderservice.http.header.HeaderGenerator;
import com.rainbowforest.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

@RestController
public class OrderController {

    @Autowired
    private UserClient userClient;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private HeaderGenerator headerGenerator;

    @GetMapping(value = "/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping(value = "/create")
    public ResponseEntity<?> createOrderFromCart(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            Order order = new Order();

            // 1. Lấy và gán User (Chìa khóa để hiện lịch sử đơn hàng thật)
            if (payload.get("userName") != null) {
                String userName = (String) payload.get("userName");
                System.out.println("Hệ thống: Đang truy vấn User - " + userName);

                User user = userClient.getUserByName(userName);
                if (user != null) {
                    order.setUser(user);
                    System.out.println("✅ Thành công: Đã gán User ID " + user.getId() + " vào đơn hàng.");
                } else {
                    System.out.println("❌ Thất bại: Không tìm thấy User '" + userName + "' trong Database.");
                }
            }

            // 2. Thiết lập thông tin đơn hàng
            order.setTotal(new BigDecimal(payload.get("total").toString()));
            order.setOrderedDate(LocalDate.now());
            order.setStatus("PENDING");

            // 3. Xử lý danh sách sản phẩm
            List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) payload.get("items");
            List<Item> items = new ArrayList<>();

            if (itemsRaw != null) {
                for (Map<String, Object> itemData : itemsRaw) {
                    Item item = new Item();
                    Long productId = Long.valueOf(itemData.get("productId").toString());
                    Product product = productClient.getProductById(productId);

                    if (product != null) {
                        item.setProduct(product);
                        item.setQuantity((Integer) itemData.get("quantity"));
                        item.setSubTotal(new BigDecimal(itemData.get("productPrice").toString())
                                .multiply(new BigDecimal(item.getQuantity())));
                        items.add(item);
                    }
                }
            }
            order.setItems(items);

            // 4. Lưu vào Database
            Order savedOrder = orderService.saveOrder(order);

            return new ResponseEntity<>(savedOrder,
                    headerGenerator.getHeadersForSuccessPostMethod(request, savedOrder.getId()), HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi server: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable("id") Long id, @RequestParam("status") String status) {
        Order order = orderService.getOrderById(id);
        if (order != null) {
            order.setStatus(status);
            order = orderService.saveOrder(order);
            return new ResponseEntity<>(order, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}