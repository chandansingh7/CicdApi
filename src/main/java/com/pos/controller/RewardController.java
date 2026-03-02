package com.pos.controller;

import com.pos.config.RewardConfig;
import com.pos.dto.response.RewardConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rewards", description = "Member reward program config and info")
@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardConfig rewardConfig;

    @GetMapping("/config")
    @Operation(summary = "Get reward program configuration")
    public ResponseEntity<RewardConfigResponse> getConfig() {
        return ResponseEntity.ok(RewardConfigResponse.builder()
                .pointsPerDollar(rewardConfig.getPointsPerDollar())
                .redemptionRate(rewardConfig.getRedemptionRate())
                .build());
    }
}
