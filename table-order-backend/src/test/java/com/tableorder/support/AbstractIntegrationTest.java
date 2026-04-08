package com.tableorder.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.common.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @Autowired
    protected TestDataFactory dataFactory;

    protected String generateAdminToken(Long storeId) {
        return jwtTokenProvider.createToken(Map.of(
                "sub", "1",
                "storeId", storeId,
                "role", "ADMIN"
        ));
    }

    protected String generateCustomerToken(Long storeId, Long tableId, Long sessionId, Long profileId) {
        return jwtTokenProvider.createToken(Map.of(
                "sub", String.valueOf(profileId),
                "storeId", storeId,
                "tableId", tableId,
                "sessionId", sessionId,
                "profileId", profileId,
                "deviceId", "test-device-" + profileId,
                "role", "CUSTOMER"
        ));
    }
}
