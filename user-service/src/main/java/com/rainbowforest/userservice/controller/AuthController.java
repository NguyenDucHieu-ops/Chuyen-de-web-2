package com.rainbowforest.userservice.controller;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.entity.UserRole;
import com.rainbowforest.userservice.service.UserService;
import com.rainbowforest.userservice.repository.UserRepository;
import com.rainbowforest.userservice.repository.UserRoleRepository;
import com.rainbowforest.userservice.dto.AdminCreateRequest;
import com.rainbowforest.userservice.service.AuditLogClient; // 🔥 1. Import "bưu tá" gửi Log
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired(required = false) // 🔥 2. Cấy thêm AuditLogClient
    private AuditLogClient auditLogClient;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            if (userRepository.findByUserName(user.getUserName()) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản đã tồn tại!");
            }

            User savedUser = userService.saveUser(user);

            // 🚀 3. Bắn Log Đăng ký
            if (auditLogClient != null) {
                auditLogClient.sendLog(savedUser.getUserName(), "CREATE", "USER-SERVICE",
                        "Đăng ký tài khoản khách hàng mới");
            }

            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    // Compatibility endpoints for Admin UI expecting /auth/* user management
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsersCompat() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUserCompat(@PathVariable Long id, @RequestBody User details) {
        try {
            User u = userService.getUserById(id);
            if (u == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");

            u.setUserName(details.getUserName());
            if (details.getUserDetails() != null) {
                details.getUserDetails().setUser(u);
                u.setUserDetails(details.getUserDetails());
            }
            if (details.getUserPassword() != null && !details.getUserPassword().isEmpty()) {
                u.setUserPassword(details.getUserPassword());
            }
            userService.saveUser(u);

            // 🚀 Bắn Log Cập nhật
            if (auditLogClient != null) {
                auditLogClient.sendLog(u.getUserName(), "UPDATE", "USER-SERVICE", "Cập nhật thông tin tài khoản");
            }

            return ResponseEntity.ok(u);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUserCompat(@PathVariable Long id) {
        try {
            userService.deleteUser(id);

            // 🚀 Bắn Log Xóa
            if (auditLogClient != null) {
                auditLogClient.sendLog("SYSTEM", "DELETE", "USER-SERVICE", "Đã xóa tài khoản có ID: " + id);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/registration")
    public ResponseEntity<?> registrationCompat(@RequestBody User user) {
        // Delegate to the existing register endpoint logic
        return register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        try {
            // 1. Lấy user từ DB
            User user = userRepository.findByUserName(loginRequest.getUserName());

            // 2. Kiểm tra tài khoản và mật khẩu
            if (user == null || loginRequest.getUserPassword() == null
                    || !loginRequest.getUserPassword().equals(user.getUserPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tài khoản hoặc mật khẩu!");
            }

            // 3. Đóng gói Token
            Map<String, Object> response = new HashMap<>();
            response.put("token", UUID.randomUUID().toString());

            // 4. BÍ QUYẾT CHỐNG VÒNG LẶP JSON
            Map<String, Object> safeUser = new HashMap<>();
            safeUser.put("id", user.getId());
            safeUser.put("userName", user.getUserName());

            if (user.getRole() != null && user.getRole().getRoleName() != null) {
                safeUser.put("roleName", user.getRole().getRoleName());
            } else {
                safeUser.put("roleName", "ROLE_USER");
            }

            response.put("user", safeUser);

            // 🛑 5. TẠM THỜI TẮT AUDIT LOG ĐỂ KHÔNG BỊ TREO MÁY (504 GATEWAY TIMEOUT) 🛑
            /*
             * if (auditLogClient != null) {
             * try {
             * auditLogClient.sendLog(user.getUserName(), "LOGIN", "USER-SERVICE",
             * "Đăng nhập thành công");
             * } catch (Exception ex) {
             * System.out.println("⚠️ Audit Service đang lỗi, bỏ qua việc ghi log.");
             * }
             * }
             */

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        }
    }

    @PostMapping("/admin/create")
    public ResponseEntity<?> createAdmin(@RequestBody AdminCreateRequest req) {
        try {
            String adminUserName = req.getAdminUserName();
            String adminPassword = req.getAdminPassword();
            User admin = userRepository.findByUserName(adminUserName);
            if (admin == null || !admin.getUserPassword().equals(adminPassword)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Admin credentials invalid");
            }

            if (admin.getRole() == null || !"ROLE_ADMIN".equals(admin.getRole().getRoleName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not admin");
            }

            User newUser = req.getUser();
            if (newUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing user payload");
            }

            if (userRepository.findByUserName(newUser.getUserName()) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản đã tồn tại!");
            }

            if (newUser.getUserPassword() == null || newUser.getUserPassword().isEmpty()) {
                newUser.setUserPassword("123456");
            }

            if (newUser.getUserDetails() != null) {
                newUser.getUserDetails().setUser(newUser);
            }

            // Tìm hoặc tạo ROLE_ADMIN
            UserRole adminRole = userRoleRepository.findUserRoleByRoleName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = new UserRole();
                adminRole.setRoleName("ROLE_ADMIN");
                adminRole = userRoleRepository.save(adminRole);
            }

            newUser.setRole(adminRole);

            User saved = userService.saveUser(newUser);

            // 🚀 Bắn Log Tạo Admin
            if (auditLogClient != null) {
                auditLogClient.sendLog(admin.getUserName(), "CREATE", "USER-SERVICE",
                        "Tạo tài khoản quản trị viên mới: " + saved.getUserName());
            }

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }
}