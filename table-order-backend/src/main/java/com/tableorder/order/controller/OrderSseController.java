package com.tableorder.order.controller;

import com.tableorder.order.sse.OrderSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/orders")
@RequiredArgsConstructor
public class OrderSseController {

    private final OrderSseService orderSseService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable Long storeId) {
        return orderSseService.subscribe(storeId);
    }
}
