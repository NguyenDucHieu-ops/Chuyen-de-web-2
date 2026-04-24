package com.rainbowforest.minigame_service.repository;

import com.rainbowforest.minigame_service.entity.SpinHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {
    // Lấy danh sách các lần quay gần nhất của 1 người
    List<SpinHistory> findByUserNameOrderBySpinTimeDesc(String userName);
}