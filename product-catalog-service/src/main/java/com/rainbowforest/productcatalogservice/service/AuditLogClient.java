package com.rainbowforest.productcatalogservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuditLogClient {

    // Gọi qua API Gateway (8900)
    private final String AUDIT_URL = "http://localhost:8900/api/history/audit";
    private RestTemplate restTemplate = new RestTemplate();

    // 🔥 ĐÂY CHÍNH LÀ CÁI HÀM MÀ VS CODE ĐANG KÊU THIẾU NÈ:
    public void sendLog(String userName, String action, String serviceName, String description) {
        try {
            Map<String, Object> logPayload = new HashMap<>();
            logPayload.put("userName", userName);
            logPayload.put("action", action);
            logPayload.put("serviceName", serviceName);
            logPayload.put("description", description);

            restTemplate.postForEntity(AUDIT_URL, logPayload, String.class);
        } catch (Exception e) {
            System.err.println(">> Lỗi không thể gửi Log: " + e.getMessage());
        }
    }
}