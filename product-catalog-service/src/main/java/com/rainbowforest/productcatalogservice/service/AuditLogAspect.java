package com.rainbowforest.productcatalogservice.service;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogClient auditLogClient;

    @Autowired
    private HttpServletRequest request;

    // 🔥 Dòng này có nghĩa là: "Camera hãy chĩa vào TẤT CẢ các file nằm trong thư
    // mục controller"
    @AfterReturning("execution(* com.rainbowforest.productcatalogservice.controller.*.*(..))")
    public void autoLog(JoinPoint joinPoint) {
        String method = request.getMethod();

        // Bỏ qua hàm GET (Xem danh sách), chỉ ghi log khi có thay đổi dữ liệu
        if (method.equals("GET")) {
            return;
        }

        // Tự động quét thẻ (Lấy Header)
        String userName = request.getHeader("X-User-Name");
        if (userName == null || userName.isEmpty()) {
            userName = "SYSTEM";
        }

        // Tự động suy ra hành động dựa trên HTTP Method
        String action = "UNKNOWN";
        if (method.equals("POST"))
            action = "CREATE";
        else if (method.equals("PUT"))
            action = "UPDATE";
        else if (method.equals("DELETE"))
            action = "DELETE";

        // Lấy tên cái hàm vừa bị gọi (VD: addProduct, deleteCategory)
        String functionName = joinPoint.getSignature().getName();
        String description = "Tự động ghi nhận hành động gọi hàm: " + functionName;

        // Bắn log đi!
        auditLogClient.sendLog(userName, action, "PRODUCT-SERVICE", description);
    }
}