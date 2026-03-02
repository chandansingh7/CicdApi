package com.pos.controller;

import com.pos.dto.request.CloseShiftRequest;
import com.pos.dto.request.OpenShiftRequest;
import com.pos.dto.response.ApiResponse;
import com.pos.dto.response.ShiftResponse;
import com.pos.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping("/open")
    public ResponseEntity<ApiResponse<ShiftResponse>> open(@Valid @RequestBody OpenShiftRequest request) {
        ShiftResponse shift = shiftService.open(request);
        return ResponseEntity.ok(ApiResponse.success(shift));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<ShiftResponse>> getCurrent() {
        ShiftResponse shift = shiftService.getCurrent();
        return ResponseEntity.ok(ApiResponse.success(shift));
    }

    @PostMapping("/close")
    public ResponseEntity<ApiResponse<ShiftResponse>> close(@Valid @RequestBody CloseShiftRequest request) {
        ShiftResponse shift = shiftService.close(request);
        return ResponseEntity.ok(ApiResponse.success(shift));
    }
}

