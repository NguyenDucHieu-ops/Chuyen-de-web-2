package com.rainbowforest.orderservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

// THÊM url = "http://localhost:8815" để gọi trực tiếp, loại bỏ lỗi tra cứu Eureka
@FeignClient(name = "payment-service", url = "http://localhost:8815")
public interface PaymentClient {

    // CHÚ Ý: Phải có value="amount" trong @RequestParam thì Feign bản cũ mới chạy
    // đúng
    @PostMapping(value = "/payment/pay/{orderId}")
    String processPayment(
            @PathVariable(value = "orderId") Long orderId,
            @RequestParam(value = "amount") BigDecimal amount);
}