package com.tableorder.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int maxRequests;
    private final ConcurrentHashMap<String, long[]> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${app.rate-limit.requests-per-minute:60}") int maxRequests) {
        this.maxRequests = maxRequests;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        long[] bucket = buckets.computeIfAbsent(clientIp, k -> new long[]{0, now});

        synchronized (bucket) {
            if (now - bucket[1] > 60_000) {
                bucket[0] = 0;
                bucket[1] = now;
            }
            bucket[0]++;
            if (bucket[0] > maxRequests) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"요청 한도를 초과했습니다\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Scheduled(fixedRate = 120_000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(e -> now - e.getValue()[1] > 120_000);
    }
}
