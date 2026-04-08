package com.tableorder.order.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class OrderSseService {

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long storeId) {
        SseEmitter emitter = new SseEmitter(57_600_000L); // 16시간
        emitters.computeIfAbsent(storeId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(storeId, emitter));
        emitter.onTimeout(() -> remove(storeId, emitter));
        emitter.onError(e -> remove(storeId, emitter));

        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data(Map.of("storeId", storeId)));
        } catch (IOException e) {
            remove(storeId, emitter);
        }
        return emitter;
    }

    public void publish(Long storeId, String eventType, Object data) {
        List<SseEmitter> storeEmitters = emitters.get(storeId);
        if (storeEmitters == null) return;

        List<SseEmitter> dead = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : storeEmitters) {
            try {
                emitter.send(SseEmitter.event().name(eventType).data(data));
            } catch (Exception e) {
                dead.add(emitter);
            }
        }
        storeEmitters.removeAll(dead);
    }

    @Scheduled(fixedRate = 30_000)
    public void heartbeat() {
        emitters.forEach((storeId, list) -> {
            List<SseEmitter> dead = new CopyOnWriteArrayList<>();
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (Exception e) {
                    dead.add(emitter);
                }
            }
            list.removeAll(dead);
            if (list.isEmpty()) emitters.remove(storeId);
        });
    }

    private void remove(Long storeId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(storeId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) emitters.remove(storeId);
        }
    }
}
