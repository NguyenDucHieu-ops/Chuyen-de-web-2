package com.rainbowforest.userservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuditLogClient {

    // Gọi sang port 8085 của Audit Service
    // Sửa cổng 8085 thành 8900, và thêm chữ /api/history vào trước /audit
    private final String AUDIT_URL = "http://localhost:8900/api/history/audit";
    private RestTemplate restTemplate = new RestTemplate();

    public void sendLog(String userName, String action, String serviceName, String description) {
        try {
            Map<String, Object> logPayload = new HashMap<>();
            logPayload.put("userName", userName);
            logPayload.put("action", action);
            logPayload.put("serviceName", serviceName);
            logPayload.put("description", description);

            restTemplate.postForEntity(AUDIT_URL, logPayload, String.class);
        } catch (Exception e) {
            System.err.println(">> Lỗi không thể gửi Log sang Audit-Service: " + e.getMessage());
        }
    }
}