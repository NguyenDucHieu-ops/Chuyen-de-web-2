package com.rainbowforest.minigame_service.controller;

import com.rainbowforest.minigame_service.entity.SpinHistory;
import com.rainbowforest.minigame_service.repository.SpinHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/minigame")
public class MinigameController {

    @Autowired
    private SpinHistoryRepository spinHistoryRepository;

    private final Random random = new Random();

    @GetMapping("/history/{userName}")
    public ResponseEntity<List<SpinHistory>> getHistory(@PathVariable String userName) {
        return ResponseEntity.ok(spinHistoryRepository.findByUserNameOrderBySpinTimeDesc(userName));
    }

    // ✅ API QUAY THƯỞNG: Đã bỏ chặn "mỗi ngày 1 lần" để sếp test và cho khách khô
    // máu
    @PostMapping("/spin")
    public ResponseEntity<?> spinWheel(@RequestParam String userName) {
        // 1. Tạo kết quả Random
        int chance = random.nextInt(100);
        int points = 0;
        String prize = "";

        if (chance < 40) {
            points = 10;
            prize = "Giải Khuyến Khích";
        } else if (chance < 70) {
            points = 20; // Sửa lại cho hấp dẫn hơn
            prize = "Giải Ba";
        } else if (chance < 95) {
            points = 100;
            prize = "Giải Nhì";
        } else {
            points = 500;
            prize = "JACKPOT";
        }

        // 2. Lưu vào database lịch sử (Luôn lưu để hiện ở cột phải)
        SpinHistory history = new SpinHistory();
        history.setUserName(userName);
        history.setPrizeName(prize);
        history.setPointsWon(points);

        try {
            SpinHistory saved = spinHistoryRepository.save(history);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            // Fix lỗi 500 nếu DB bị kẹt
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi lưu lịch sử!");
        }
    }
}