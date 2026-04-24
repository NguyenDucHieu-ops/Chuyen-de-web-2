package com.rainbowforest.reward_service.repository;

import com.rainbowforest.reward_service.entity.RewardWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardWalletRepository extends JpaRepository<RewardWallet, Long> {
    // Tìm cái ví điểm thưởng dựa theo tên khách hàng
    RewardWallet findByUserName(String userName);
}