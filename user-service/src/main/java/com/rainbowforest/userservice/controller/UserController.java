package com.rainbowforest.userservice.controller;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.service.UserService;
import com.rainbowforest.userservice.service.EmailService; // 1. Import Email Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserService userService;

	// 2. Tiêm (Inject) EmailService vào
	@Autowired
	private EmailService emailService;

	@GetMapping
	public ResponseEntity<List<User>> getAllUsers() {
		return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
	}

	@GetMapping("/name/{userName}")
	public ResponseEntity<User> getUserByName(@PathVariable String userName) {
		User user = userService.getUserByName(userName);
		if (user != null) {
			return new ResponseEntity<>(user, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PostMapping
	public ResponseEntity<User> addUser(@RequestBody User user) {
		return new ResponseEntity<>(userService.saveUser(user), HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User details) {
		User u = userService.getUserById(id);
		if (u != null) {
			u.setUserName(details.getUserName());
			if (details.getUserDetails() != null) {
				// Gán 2 chiều để đảm bảo quan hệ OneToOne có tham chiếu ngược về User
				u.setUserDetails(details.getUserDetails());
				details.getUserDetails().setUser(u);
			}
			userService.saveUser(u);
			return new ResponseEntity<>(u, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	// ==========================================
	// TÍNH NĂNG MỚI: QUÊN MẬT KHẨU
	// ==========================================
	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
		try {
			String userName = request.get("userName");

			User user = userService.getUserByName(userName);
			if (user == null) {
				return ResponseEntity.badRequest().body("Không tìm thấy tài khoản với tên đăng nhập này!");
			}

			// Lấy email từ UserDetails (Vì email nằm trong bảng user_details)
			String userEmail = null;
			if (user.getUserDetails() != null) {
				userEmail = user.getUserDetails().getEmail();
			}

			if (userEmail == null || userEmail.trim().isEmpty()) {
				return ResponseEntity.badRequest()
						.body("Tài khoản này chưa cập nhật địa chỉ Email, không thể lấy lại mật khẩu!");
			}

			// Tạo một mật khẩu mới ngẫu nhiên (Ví dụ: FIXT-a1b2c3)
			String newRandomPassword = "FIXT-" + UUID.randomUUID().toString().substring(0, 6);

			// Cập nhật lại mật khẩu cho user
			user.setUserPassword(newRandomPassword);
			userService.saveUser(user);

			// GỌI HÀM GỬI EMAIL TỰ ĐỘNG
			emailService.sendForgotPasswordEmail(userEmail, user.getUserName(), newRandomPassword);

			return ResponseEntity.ok("Mật khẩu mới đã được gửi đến địa chỉ Email liên kết với tài khoản!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi khi gửi email!");
		}
	}

	// ==========================================
	// TÍNH NĂNG MỚI: ĐỔI MẬT KHẨU TỪ PROFILE
	// ==========================================
	@PostMapping("/change-password")
	public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
		try {
			String userName = request.get("userName");
			String oldPassword = request.get("oldPassword");
			String newPassword = request.get("newPassword");

			User user = userService.getUserByName(userName);
			if (user == null) {
				return ResponseEntity.badRequest().body("Tài khoản không tồn tại!");
			}

			// Kiểm tra mật khẩu cũ
			if (!user.getUserPassword().equals(oldPassword)) {
				return ResponseEntity.badRequest().body("Mật khẩu cũ không chính xác!");
			}

			// Cập nhật mật khẩu mới
			user.setUserPassword(newPassword);
			userService.saveUser(user);

			// GỬI EMAIL THÔNG BÁO BẢO MẬT NẾU CÓ EMAIL
			if (user.getUserDetails() != null && user.getUserDetails().getEmail() != null) {
				emailService.sendPasswordChangedEmail(user.getUserDetails().getEmail(), userName);
			}

			return ResponseEntity.ok("Đổi mật khẩu thành công!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi xử lý yêu cầu!");
		}
	}
}