package com.rainbowforest.reward_service.controller;

import com.rainbowforest.reward_service.entity.RewardWallet;
import com.rainbowforest.reward_service.repository.RewardWalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rewards") // Gateway đã map sẵn /api/rewards vào đây rồi
public class RewardController {

    @Autowired
    private RewardWalletRepository walletRepository;

    // 1. API: Lấy số điểm hiện tại của một người dùng
    @GetMapping("/{userName}")
    public ResponseEntity<?> getPoints(@PathVariable String userName) {
        RewardWallet wallet = walletRepository.findByUserName(userName);
        if (wallet == null) {
            // Nếu chưa có ví, tạo luôn cho họ một cái ví 0 điểm
            wallet = new RewardWallet(userName, 0);
            wallet = walletRepository.save(wallet);
        }
        return ResponseEntity.ok(wallet);
    }

    // 2. API: Cộng điểm (Khi mua hàng xong)
    @PostMapping("/add")
    public ResponseEntity<?> addPoints(@RequestParam String userName, @RequestParam int points) {
        RewardWallet wallet = walletRepository.findByUserName(userName);
        if (wallet == null)
            wallet = new RewardWallet(userName, 0);

        wallet.setTotalPoints(wallet.getTotalPoints() + points);
        walletRepository.save(wallet);

        return ResponseEntity.ok(wallet);
    }

    // 3. API: Trừ điểm (Khi khách dùng điểm thanh toán)
    @PostMapping("/use")
    public ResponseEntity<?> usePoints(@RequestParam String userName, @RequestParam int pointsToUse) {
        RewardWallet wallet = walletRepository.findByUserName(userName);

        if (wallet == null || wallet.getTotalPoints() < pointsToUse) {
            return ResponseEntity.badRequest().body("Không đủ điểm thưởng để thanh toán!");
        }

        wallet.setTotalPoints(wallet.getTotalPoints() - pointsToUse);
        walletRepository.save(wallet);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đã dùng " + pointsToUse + " điểm thành công.");
        response.put("remainingPoints", wallet.getTotalPoints());
        return ResponseEntity.ok(response);
    }
}