package com.rainbowforest.payment_service.controller;

import com.rainbowforest.payment_service.config.VNPAYConfig;
import com.rainbowforest.payment_service.domain.Transaction;
import com.rainbowforest.payment_service.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

@RestController
// ✅ ĐÃ XÓA @RequestMapping để Gateway StripPrefix khớp trực tiếp
public class PaymentController {

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/create-vnpay-link")
    public ResponseEntity<?> createPayment(
            @RequestParam("amount") long amount,
            @RequestParam("orderId") String orderId) {

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPAYConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VNPAYConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", VNPAYConfig.vnp_TmnCode);
        // Trong PaymentController.java
        // Sửa dòng này để ép số tiền lên mức tối thiểu của Sandbox (ví dụ 100,000 VND)
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100 * 35000)); // Nhân thêm tỷ giá để ra tiền Việt
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", orderId + "_" + System.currentTimeMillis());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPAYConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPAYConfig.hmacSHA512(VNPAYConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPAYConfig.vnp_PayUrl + "?" + queryUrl;

        return ResponseEntity.ok(Collections.singletonMap("url", paymentUrl));
    }

    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> paymentCallback(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // Tính toán lại mã băm để so sánh
        String signValue = VNPAYConfig.hashAllFields(fields); // Em cần thêm hàm hashAllFields vào VNPAYConfig

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                return ResponseEntity.ok(Collections.singletonMap("status", "SUCCESS"));
            } else {
                return ResponseEntity.ok(Collections.singletonMap("status", "FAILED"));
            }
        } else {
            return ResponseEntity.badRequest().body("Mã băm không hợp lệ");
        }
    }

    @PostMapping("/pay/{orderId}")
    public ResponseEntity<String> processPayment(
            @PathVariable Long orderId,
            @RequestParam(value = "amount") BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setOrderId(orderId);
        transaction.setAmount(amount);
        transaction.setPaymentDate(LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);
        return ResponseEntity.ok("PAYMENT_SUCCESS");
    }
}