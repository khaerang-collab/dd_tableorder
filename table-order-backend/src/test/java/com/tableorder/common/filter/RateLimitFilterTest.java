package com.tableorder.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(5); // 분당 5회 제한
        filterChain = mock(FilterChain.class);
    }

    // UT-RATE-001: Rate Limit 이내 요청 허용
    @Test
    @DisplayName("Rate Limit 이내의 요청은 정상 통과한다")
    void requestWithinLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    // UT-RATE-002: Rate Limit 초과 시 429 응답
    @Test
    @DisplayName("Rate Limit 초과 시 429 Too Many Requests를 반환한다")
    void requestExceedsLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");

        // 5회 요청 (한도 내)
        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(request, resp, filterChain);
        }

        // 6번째 요청 (한도 초과)
        MockHttpServletResponse response = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("RATE_LIMITED");
    }
}
