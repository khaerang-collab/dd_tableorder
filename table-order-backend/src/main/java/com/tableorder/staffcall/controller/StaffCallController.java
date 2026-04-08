package com.tableorder.staffcall.controller;

import com.tableorder.order.sse.OrderSseService;
import com.tableorder.staffcall.dto.StaffCallRequest;
import com.tableorder.staffcall.dto.StaffCallResponse;
import com.tableorder.staffcall.service.StaffCallService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StaffCallController {

    private final StaffCallService staffCallService;
    private final OrderSseService sseService;

    // 고객: 직원 호출
    @PostMapping("/api/customer/stores/{storeId}/staff-call")
    public ResponseEntity<StaffCallResponse> createCall(@PathVariable Long storeId,
                                                         @Valid @RequestBody StaffCallRequest request) {
        StaffCallResponse call = staffCallService.createCall(storeId, request);
        sseService.publish(storeId, "STAFF_CALL", Map.of(
                "callId", call.id(), "tableNumber", call.tableNumber(), "reason", call.reason()));
        return ResponseEntity.ok(call);
    }

    // 관리자: 대기 중 호출 목록
    @GetMapping("/api/admin/stores/{storeId}/staff-calls")
    public ResponseEntity<List<StaffCallResponse>> getPendingCalls(@PathVariable Long storeId) {
        return ResponseEntity.ok(staffCallService.getPendingCalls(storeId));
    }

    // 관리자: 호출 확인 처리
    @PostMapping("/api/admin/staff-calls/{callId}/resolve")
    public ResponseEntity<Map<String, Boolean>> resolveCall(@PathVariable Long callId) {
        staffCallService.resolveCall(callId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
