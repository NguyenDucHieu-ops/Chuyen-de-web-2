package com.rainbowforest.reward_service.entity;

import javax.persistence.*;

@Entity
@Table(name = "reward_wallets")
public class RewardWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userName; // Tên khách hàng

    private int totalPoints; // Tổng điểm hiện có

    // Constructor mặc định
    public RewardWallet() {
    }

    public RewardWallet(String userName, int totalPoints) {
        this.userName = userName;
        this.totalPoints = totalPoints;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}