package com.tableorder.staffcall.service;

import com.tableorder.common.exception.BusinessException;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.staffcall.dto.StaffCallRequest;
import com.tableorder.staffcall.dto.StaffCallResponse;
import com.tableorder.staffcall.entity.StaffCall;
import com.tableorder.staffcall.repository.StaffCallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffCallService {

    private final StaffCallRepository repository;

    @Transactional
    public StaffCallResponse createCall(Long storeId, StaffCallRequest request) {
        // 30초 쿨다운
        boolean recentCall = repository.existsByTableIdAndStatusAndCreatedAtAfter(
                request.tableId(), StaffCall.PENDING, LocalDateTime.now().minusSeconds(30));
        if (recentCall) {
            throw new CooldownException();
        }

        StaffCall call = repository.save(StaffCall.builder()
                .storeId(storeId).tableId(request.tableId()).tableNumber(request.tableNumber())
                .reason(request.reason()).customMessage(request.customMessage()).build());
        return toResponse(call);
    }

    public List<StaffCallResponse> getPendingCalls(Long storeId) {
        return repository.findByStoreIdAndStatusOrderByCreatedAtDesc(storeId, StaffCall.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void resolveCall(Long callId) {
        StaffCall call = repository.findById(callId)
                .orElseThrow(() -> new NotFoundException("호출을 찾을 수 없습니다"));
        call.setStatus(StaffCall.RESOLVED);
        call.setResolvedAt(LocalDateTime.now());
    }

    private StaffCallResponse toResponse(StaffCall c) {
        return new StaffCallResponse(c.getId(), c.getTableId(), c.getTableNumber(),
                c.getReason(), c.getCustomMessage(), c.getStatus(), c.getCreatedAt());
    }

    static class CooldownException extends BusinessException {
        CooldownException() { super("COOLDOWN", "30초 후에 다시 호출해주세요"); }
    }
}
