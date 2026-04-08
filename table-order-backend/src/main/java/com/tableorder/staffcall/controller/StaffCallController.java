package com.tableorder.staffcall.controller;

import com.tableorder.staffcall.dto.StaffCallRequest;
import com.tableorder.staffcall.dto.StaffCallResponse;
import com.tableorder.staffcall.service.StaffCallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StaffCallController {

    private final StaffCallService staffCallService;

    // 고객: 직원 호출
    @PostMapping("/api/customer/sessions/{sessionId}/staff-calls")
    public ResponseEntity<StaffCallResponse> createCall(
            @PathVariable Long sessionId,
            @Valid @RequestBody StaffCallRequest request) {
        return ResponseEntity.ok(staffCallService.createCall(sessionId, request.reason(), request.message()));
    }

    // 관리자: 대기 중인 호출 목록
    @GetMapping("/api/admin/stores/{storeId}/staff-calls")
    public ResponseEntity<List<StaffCallResponse>> getCalls(@PathVariable Long storeId) {
        return ResponseEntity.ok(staffCallService.getPendingCalls(storeId));
    }

    // 관리자: 호출 응대 처리
    @PutMapping("/api/admin/staff-calls/{callId}/attend")
    public ResponseEntity<StaffCallResponse> attendCall(@PathVariable Long callId) {
        return ResponseEntity.ok(staffCallService.attendCall(callId));
    }
}
