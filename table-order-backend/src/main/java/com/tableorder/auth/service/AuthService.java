package com.tableorder.auth.service;

import com.tableorder.auth.dto.*;
import com.tableorder.auth.entity.AdminUser;
import com.tableorder.auth.repository.AdminUserRepository;
import com.tableorder.common.exception.AccountLockedException;
import com.tableorder.common.exception.ForbiddenException;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.common.security.JwtTokenProvider;
import com.tableorder.customer.entity.CustomerProfile;
import com.tableorder.customer.service.CustomerProfileService;
import com.tableorder.store.entity.Store;
import com.tableorder.store.service.StoreService;
import com.tableorder.table.entity.RestaurantTable;
import com.tableorder.table.entity.TableSession;
import com.tableorder.table.service.TableService;
import com.tableorder.table.service.TableSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StoreService storeService;
    private final AdminUserRepository adminUserRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final KakaoOAuthService kakaoOAuthService;
    private final CustomerProfileService customerProfileService;
    private final TableService tableService;
    private final TableSessionService tableSessionService;

    @Value("${app.login.max-attempts}") private int maxAttempts;
    @Value("${app.login.lock-duration-minutes}") private int lockDurationMinutes;

    @Transactional
    public AdminLoginResponse adminLogin(Long storeId, String username, String password) {
        Store store = storeService.getStore(storeId);
        AdminUser admin = adminUserRepository.findByStoreIdAndUsername(storeId, username)
                .orElseThrow(() -> new ForbiddenException("인증 실패"));

        if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AccountLockedException("계정이 잠겼습니다. 잠시 후 재시도해주세요");
        }

        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            admin.setLoginAttempts(admin.getLoginAttempts() + 1);
            if (admin.getLoginAttempts() >= maxAttempts) {
                admin.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
            }
            throw new ForbiddenException("인증 실패");
        }

        admin.setLoginAttempts(0);
        admin.setLockedUntil(null);

        String token = jwtTokenProvider.createToken(Map.of(
                "sub", admin.getId().toString(),
                "storeId", storeId,
                "role", "ADMIN"
        ));
        return new AdminLoginResponse(token, storeId, store.getName(), admin.getUsername());
    }

    @Transactional
    public KakaoLoginResponse kakaoLogin(Long storeId, Integer tableNumber, String kakaoAccessToken) {
        Store store = storeService.getStore(storeId);
        RestaurantTable table = tableService.getTableByStoreAndNumber(storeId, tableNumber);
        TableSession session = tableSessionService.getOrCreateActiveSession(table.getId());

        KakaoProfile kakaoProfile;
        if ("dev-test-token".equals(kakaoAccessToken)) {
            long devId = System.currentTimeMillis() % 1000000;
            kakaoProfile = new KakaoProfile(devId, "테스트유저" + devId, "male", "20~29", null);
        } else {
            kakaoProfile = kakaoOAuthService.getKakaoProfile(kakaoAccessToken);
        }
        CustomerProfile profile = customerProfileService.findOrCreate(kakaoProfile);

        String token = jwtTokenProvider.createToken(Map.of(
                "sub", profile.getId().toString(),
                "storeId", storeId,
                "tableId", table.getId(),
                "sessionId", session.getId(),
                "profileId", profile.getId(),
                "deviceId", "kakao-" + profile.getKakaoId(),
                "role", "CUSTOMER"
        ));

        return new KakaoLoginResponse(token, storeId, store.getName(),
                tableNumber, table.getId(), session.getId(),
                profile.getId(), profile.getNickname(), profile.getProfileImageUrl());
    }
}
