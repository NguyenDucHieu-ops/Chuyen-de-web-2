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

            // ✅ 1. LẤY THÔNG TIN TỪ PAYLOAD VÀ LƯU VÀO ENTITY (Sửa lỗi N/A)
            if (payload.get("customerName") != null)
                order.setCustomerName((String) payload.get("customerName"));
            if (payload.get("phoneNumber") != null)
                order.setPhoneNumber((String) payload.get("phoneNumber"));
            if (payload.get("shippingAddress") != null)
                order.setShippingAddress((String) payload.get("shippingAddress"));
            if (payload.get("paymentMethod") != null)
                order.setPaymentMethod((String) payload.get("paymentMethod"));

            // 2. Lấy và gán User
            if (payload.get("userName") != null) {
                String userName = (String) payload.get("userName");
                User user = userClient.getUserByName(userName);
                if (user != null) {
                    order.setUser(user);
                    order.setUserName(userName);
                }
            }

            // 3. Thiết lập thông tin đơn hàng
            if (payload.get("total") != null) {
                order.setTotal(new BigDecimal(payload.get("total").toString()));
            }
            order.setOrderedDate(LocalDate.now());
            order.setStatus("PENDING");

            // 4. Xử lý danh sách sản phẩm
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

                        // ✅ LƯU TÊN, ẢNH VÀ GIÁ VÀO BẢNG ITEM ĐỂ HIỆN LỊCH SỬ (Sửa lỗi 0đ)
                        if (itemData.get("productName") != null)
                            item.setProductName((String) itemData.get("productName"));
                        if (itemData.get("productImageUrl") != null)
                            item.setProductImageUrl((String) itemData.get("productImageUrl"));
                        if (itemData.get("productPrice") != null) {
                            BigDecimal price = new BigDecimal(itemData.get("productPrice").toString());
                            item.setProductPrice(price);
                            item.setSubTotal(price.multiply(new BigDecimal(item.getQuantity())));
                        }
                        items.add(item);
                    }
                }
            }
            order.setItems(items);

            // 5. Lưu vào Database
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