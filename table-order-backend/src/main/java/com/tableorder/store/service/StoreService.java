package com.tableorder.store.service;

import com.tableorder.common.exception.NotFoundException;
import com.tableorder.store.entity.Store;
import com.tableorder.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    public Store getStore(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("매장을 찾을 수 없습니다: " + id));
    }
}
