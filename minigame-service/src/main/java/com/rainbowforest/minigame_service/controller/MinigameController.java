package com.rainbowforest.minigame_service.controller;

import com.rainbowforest.minigame_service.entity.SpinHistory;
import com.rainbowforest.minigame_service.repository.SpinHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/minigame")
public class MinigameController {

    @Autowired
    private SpinHistoryRepository spinHistoryRepository;

    private final Random random = new Random();

    // Lấy lịch sử quay của user
    @GetMapping("/history/{userName}")
    public ResponseEntity<List<SpinHistory>> getHistory(@PathVariable String userName) {
        return ResponseEntity.ok(spinHistoryRepository.findByUserNameOrderBySpinTimeDesc(userName));
    }

    // API thực hiện quay thưởng (ĐÃ BỌC GIÁP CHỐNG HACK)
    @PostMapping("/spin")
    public ResponseEntity<?> spinWheel(@RequestParam String userName) {

        // --- BƯỚC 1: KIỂM TRA ĐÃ QUAY HÔM NAY CHƯA ---
        List<SpinHistory> recentSpins = spinHistoryRepository.findByUserNameOrderBySpinTimeDesc(userName);
        if (!recentSpins.isEmpty()) {
            LocalDate lastSpinDate = recentSpins.get(0).getSpinTime().toLocalDate();
            // Nếu ngày quay cuối cùng chính là ngày hôm nay -> Chặn lại ngay!
            if (lastSpinDate.isEqual(LocalDate.now())) {
                return ResponseEntity.badRequest().body("HẾT LƯỢT! Bạn đã quay hôm nay rồi, ngày mai quay lại nhé!");
            }
        }

        // --- BƯỚC 2: NẾU CHƯA QUAY THÌ CHO RANDOM NHƯ CŨ ---
        int chance = random.nextInt(100); // Random từ 0 đến 99
        int points = 0;
        String prize = "";

        if (chance < 40) {
            points = 10;
            prize = "Giải Khuyến Khích: 10 Điểm";
        } else if (chance < 70) {
            points = 50;
            prize = "Giải Ba: 50 Điểm";
        } else if (chance < 90) {
            points = 100;
            prize = "Giải Nhì: 100 Điểm";
        } else {
            points = 500;
            prize = "JACKPOT: 500 Điểm";
        }

        // Lưu vào database lịch sử
        SpinHistory history = new SpinHistory();
        history.setUserName(userName);
        history.setPrizeName(prize);
        history.setPointsWon(points);
        spinHistoryRepository.save(history);

        return ResponseEntity.ok(history);
    }
}