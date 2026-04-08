package com.tableorder.table.service;

import com.tableorder.common.exception.NotFoundException;
import com.tableorder.store.entity.Store;
import com.tableorder.store.service.StoreService;
import com.tableorder.table.entity.RestaurantTable;
import com.tableorder.table.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final StoreService storeService;

    @Transactional
    public RestaurantTable createTable(Long storeId, Integer tableNumber, String baseUrl) {
        Store store = storeService.getStore(storeId);
        String qrUrl = String.format("%s/customer?storeId=%d&table=%d", baseUrl, storeId, tableNumber);
        RestaurantTable table = RestaurantTable.builder()
                .store(store).tableNumber(tableNumber).qrCodeUrl(qrUrl).build();
        return tableRepository.save(table);
    }

    public List<RestaurantTable> getTables(Long storeId) {
        return tableRepository.findByStoreIdOrderByTableNumber(storeId);
    }

    public RestaurantTable getTableByStoreAndNumber(Long storeId, Integer tableNumber) {
        return tableRepository.findByStoreIdAndTableNumber(storeId, tableNumber)
                .orElseThrow(() -> new NotFoundException("테이블을 찾을 수 없습니다"));
    }
}
