package com.rainbowforest.minigame_service.entity;

import javax.persistence.*;

@Entity
@Table(name = "prizes")
public class Prize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Tên giải (VD: "Voucher 50K", "Trúng 100 Điểm")

    private String type; // Loại giải: "POINT", "VOUCHER", "GIFT", "NOTHING"

    private String rewardValue; // Giá trị: Nếu là POINT thì lưu "100", nếu VOUCHER thì lưu mã "GIAM50K"

    private int probability; // Tỷ lệ trúng (%)

    private boolean isActive = true; // Giải này đang được bật hay tắt?

    // Getter và Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRewardValue() {
        return rewardValue;
    }

    public void setRewardValue(String rewardValue) {
        this.rewardValue = rewardValue;
    }

    public int getProbability() {
        return probability;
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}