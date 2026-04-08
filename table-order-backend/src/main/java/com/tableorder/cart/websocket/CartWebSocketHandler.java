package com.tableorder.cart.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartWebSocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long sessionId = extractSessionId(session);
        if (sessionId != null) {
            sessionMap.computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>()).add(session);
            int count = sessionMap.get(sessionId).size();
            broadcast(sessionId, "USER_JOINED", Map.of("activeUserCount", count));
            log.info("WebSocket connected: sessionId={}, connections={}", sessionId, count);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long sessionId = extractSessionId(session);
        if (sessionId != null) {
            Set<WebSocketSession> sessions = sessionMap.get(sessionId);
            if (sessions != null) {
                sessions.remove(session);
                int count = sessions.size();
                if (count == 0) sessionMap.remove(sessionId);
                else broadcast(sessionId, "USER_LEFT", Map.of("activeUserCount", count));
            }
        }
    }

    public void broadcast(Long sessionId, String type, Object data) {
        Set<WebSocketSession> sessions = sessionMap.get(sessionId);
        if (sessions == null) return;
        try {
            String json = objectMapper.writeValueAsString(Map.of("type", type, "data", data));
            TextMessage message = new TextMessage(json);
            for (WebSocketSession ws : sessions) {
                if (ws.isOpen()) {
                    try { ws.sendMessage(message); }
                    catch (Exception e) { log.warn("Failed to send WS message", e); }
                }
            }
        } catch (Exception e) {
            log.error("Failed to serialize WS event", e);
        }
    }

    public void broadcastSessionComplete(Long tableId) {
        sessionMap.forEach((sessionId, sessions) ->
                broadcast(sessionId, "SESSION_COMPLETED", Map.of("tableId", tableId)));
    }

    private Long extractSessionId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String path = uri.getPath();
        String[] parts = path.split("/");
        try { return Long.parseLong(parts[parts.length - 1]); }
        catch (NumberFormatException e) { return null; }
    }
}
