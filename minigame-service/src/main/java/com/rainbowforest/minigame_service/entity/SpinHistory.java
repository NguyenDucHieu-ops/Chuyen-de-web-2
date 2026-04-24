package com.rainbowforest.minigame_service.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "spin_history")
public class SpinHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName; // Ai quay?
    private String prizeName; // Trúng giải gì (VD: "100 Điểm", "Chúc may mắn lần sau")
    private int pointsWon; // Số điểm thực tế nhận được
    
    private LocalDateTime spinTime;

    @PrePersist
    protected void onCreate() {
        this.spinTime = LocalDateTime.now();
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPrizeName() { return prizeName; }
    public void setPrizeName(String prizeName) { this.prizeName = prizeName; }
    public int getPointsWon() { return pointsWon; }
    public void setPointsWon(int pointsWon) { this.pointsWon = pointsWon; }
    public LocalDateTime getSpinTime() { return spinTime; }
    public void setSpinTime(LocalDateTime spinTime) { this.spinTime = spinTime; }
}