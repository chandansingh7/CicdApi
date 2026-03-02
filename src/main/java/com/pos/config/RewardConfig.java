package com.pos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RewardConfig {

    @Value("${reward.points-per-dollar:1}")
    private int pointsPerDollar;

    @Value("${reward.redemption-rate:100}")
    private int redemptionRate;

    public int getPointsPerDollar() {
        return pointsPerDollar;
    }

    public int getRedemptionRate() {
        return redemptionRate;
    }
}
