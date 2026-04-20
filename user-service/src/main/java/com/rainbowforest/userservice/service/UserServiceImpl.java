package com.rainbowforest.userservice.service;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.entity.UserDetails;
import com.rainbowforest.userservice.entity.UserRole;
import com.rainbowforest.userservice.repository.UserRepository;
import com.rainbowforest.userservice.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public User saveUser(User user) {
        user.setActive(1);

        if (user.getUserPassword() == null || user.getUserPassword().isEmpty()) {
            user.setUserPassword("123456");
        }

        // 🛡️ 1. Gán Details và liên kết 2 chiều
        if (user.getUserDetails() == null) {
            UserDetails details = new UserDetails();
            details.setFirstName("Thành viên");
            details.setLastName("Mới");
            details.setEmail(user.getUserName() + "@fixt.com");

            user.setUserDetails(details);
            details.setUser(user); // 🚩 Dòng này giúp bảng users_details có ID
        }

        // 🛡️ 2. Gán role mặc định chỉ khi user chưa có role
        if (user.getRole() == null) {
            // Ưu tiên tìm theo tên vai trò, fallback sang id=2, nếu không có thì tạo mới
            UserRole role = userRoleRepository.findUserRoleByRoleName("ROLE_USER");
            if (role == null) {
                role = userRoleRepository.findById(2L).orElse(null);
            }
            if (role == null) {
                role = new UserRole();
                role.setRoleName("ROLE_USER");
                role = userRoleRepository.save(role);
            }
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}