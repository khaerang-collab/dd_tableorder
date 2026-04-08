package com.tableorder.staffcall.service;

import com.tableorder.common.exception.NotFoundException;
import com.tableorder.order.sse.OrderSseService;
import com.tableorder.staffcall.dto.StaffCallResponse;
import com.tableorder.staffcall.entity.StaffCall;
import com.tableorder.staffcall.repository.StaffCallRepository;
import com.tableorder.table.entity.RestaurantTable;
import com.tableorder.table.entity.TableSession;
import com.tableorder.table.repository.TableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StaffCallService {

    private final StaffCallRepository staffCallRepository;
    private final TableSessionRepository sessionRepository;
    private final OrderSseService orderSseService;

    @Transactional
    public StaffCallResponse createCall(Long sessionId, String reason, String message) {
        TableSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("세션을 찾을 수 없습니다"));
        RestaurantTable table = session.getTable();
        Long storeId = table.getStore().getId();

        StaffCall call = StaffCall.builder()
                .storeId(storeId)
                .tableId(table.getId())
                .tableNumber(table.getTableNumber())
                .sessionId(sessionId)
                .reason(reason)
                .message(message)
                .build();

        staffCallRepository.save(call);

        // SSE로 관리자에게 알림
        orderSseService.publish(storeId, "STAFF_CALL", Map.of(
                "callId", call.getId(),
                "tableNumber", call.getTableNumber(),
                "reason", call.getReason(),
                "message", call.getMessage() != null ? call.getMessage() : "",
                "calledAt", call.getCalledAt().toString()
        ));

        return toResponse(call);
    }

    public List<StaffCallResponse> getCallsByStore(Long storeId) {
        return staffCallRepository.findByStoreIdOrderByCalledAtDesc(storeId)
                .stream().map(this::toResponse).toList();
    }

    public List<StaffCallResponse> getPendingCalls(Long storeId) {
        return staffCallRepository.findByStoreIdAndStatusOrderByCalledAtDesc(storeId, StaffCall.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public StaffCallResponse attendCall(Long callId) {
        StaffCall call = staffCallRepository.findById(callId)
                .orElseThrow(() -> new NotFoundException("호출을 찾을 수 없습니다"));
        call.setStatus(StaffCall.ATTENDED);
        call.setAttendedAt(LocalDateTime.now());
        return toResponse(call);
    }

    private StaffCallResponse toResponse(StaffCall c) {
        return new StaffCallResponse(c.getId(), c.getTableId(), c.getTableNumber(),
                c.getReason(), c.getMessage(), c.getStatus(), c.getCalledAt(), c.getAttendedAt());
    }
}
