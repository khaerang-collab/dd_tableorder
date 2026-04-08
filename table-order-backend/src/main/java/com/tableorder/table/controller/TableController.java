package com.tableorder.table.controller;

import com.tableorder.cart.websocket.CartWebSocketHandler;
import com.tableorder.order.sse.OrderSseService;
import com.tableorder.table.dto.CreateTableRequest;
import com.tableorder.table.dto.TableResponse;
import com.tableorder.table.entity.RestaurantTable;
import com.tableorder.table.entity.TableSession;
import com.tableorder.table.service.TableService;
import com.tableorder.table.service.TableSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;
    private final TableSessionService tableSessionService;
    private final CartWebSocketHandler cartWebSocketHandler;
    private final OrderSseService orderSseService;

    @PostMapping
    public ResponseEntity<TableResponse> createTable(@PathVariable Long storeId,
                                                     @Valid @RequestBody CreateTableRequest request) {
        RestaurantTable table = tableService.createTable(storeId, request.tableNumber(),
                request.baseUrl() != null ? request.baseUrl() : "");
        return ResponseEntity.ok(new TableResponse(table.getId(), storeId, table.getTableNumber(), table.getQrCodeUrl()));
    }

    @GetMapping
    public ResponseEntity<List<TableResponse>> getTables(@PathVariable Long storeId) {
        List<TableResponse> tables = tableService.getTables(storeId).stream()
                .map(t -> new TableResponse(t.getId(), storeId, t.getTableNumber(), t.getQrCodeUrl()))
                .toList();
        return ResponseEntity.ok(tables);
    }

    @PostMapping("/{tableId}/complete")
    public ResponseEntity<Map<String, Boolean>> completeSession(@PathVariable Long storeId,
                                                                 @PathVariable Long tableId) {
        tableSessionService.completeSession(storeId, tableId);
        cartWebSocketHandler.broadcastSessionComplete(tableId);
        orderSseService.publish(storeId, "TABLE_COMPLETED", Map.of("tableId", tableId));
        return ResponseEntity.ok(Map.of("success", true));
    }
}
