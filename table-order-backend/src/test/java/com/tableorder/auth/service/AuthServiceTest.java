package com.tableorder.auth.service;

import com.tableorder.auth.dto.AdminLoginResponse;
import com.tableorder.auth.dto.KakaoLoginResponse;
import com.tableorder.auth.dto.KakaoProfile;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private StoreService storeService;
    @Mock private AdminUserRepository adminUserRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private KakaoOAuthService kakaoOAuthService;
    @Mock private CustomerProfileService customerProfileService;
    @Mock private TableService tableService;
    @Mock private TableSessionService tableSessionService;

    private Store store;
    private AdminUser adminUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "maxAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockDurationMinutes", 15);

        store = Store.builder().id(1L).name("테스트 매장").build();
        adminUser = AdminUser.builder()
                .id(1L)
                .storeId(1L)
                .username("admin")
                .passwordHash("$2a$10$encoded")
                .loginAttempts(0)
                .build();
    }

    // UT-AUTH-001: 관리자 로그인 성공
    @Test
    @DisplayName("올바른 자격증명으로 관리자 로그인에 성공한다")
    void adminLoginSuccess() {
        when(adminUserRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("admin1234", "$2a$10$encoded")).thenReturn(true);
        when(storeService.getStore(1L)).thenReturn(store);
        when(jwtTokenProvider.createToken(anyMap())).thenReturn("jwt-token");

        AdminLoginResponse response = authService.adminLogin(1L, "admin", "admin1234");

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.storeId()).isEqualTo(1L);
        assertThat(response.storeName()).isEqualTo("테스트 매장");
        assertThat(response.adminUsername()).isEqualTo("admin");
        assertThat(adminUser.getLoginAttempts()).isZero();
    }

    // UT-AUTH-002: 관리자 로그인 실패 - 잘못된 비밀번호
    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 ForbiddenException이 발생하고 loginAttempts가 증가한다")
    void adminLoginWrongPassword() {
        when(adminUserRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("wrong", "$2a$10$encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.adminLogin(1L, "admin", "wrong"))
                .isInstanceOf(ForbiddenException.class);

        assertThat(adminUser.getLoginAttempts()).isEqualTo(1);
    }

    // UT-AUTH-003: 관리자 로그인 5회 실패 시 계정 잠금
    @Test
    @DisplayName("5회 연속 로그인 실패 시 계정이 잠긴다")
    void adminLoginLockAfter5Failures() {
        adminUser.setLoginAttempts(4); // 이미 4회 실패
        when(adminUserRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("wrong", "$2a$10$encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.adminLogin(1L, "admin", "wrong"))
                .isInstanceOf(ForbiddenException.class);

        assertThat(adminUser.getLoginAttempts()).isEqualTo(5);
        assertThat(adminUser.getLockedUntil()).isNotNull();
        assertThat(adminUser.getLockedUntil()).isAfter(LocalDateTime.now());
    }

    // UT-AUTH-004: 잠긴 계정으로 로그인 시도
    @Test
    @DisplayName("잠긴 계정으로 로그인하면 AccountLockedException이 발생한다")
    void adminLoginLockedAccount() {
        adminUser.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(adminUserRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> authService.adminLogin(1L, "admin", "admin1234"))
                .isInstanceOf(AccountLockedException.class);
    }

    // UT-AUTH-005: 잠금 시간 경과 후 로그인 성공
    @Test
    @DisplayName("잠금 시간 경과 후에는 정상 로그인이 가능하다")
    void adminLoginAfterLockExpiry() {
        adminUser.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        adminUser.setLoginAttempts(5);
        when(adminUserRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("admin1234", "$2a$10$encoded")).thenReturn(true);
        when(storeService.getStore(1L)).thenReturn(store);
        when(jwtTokenProvider.createToken(anyMap())).thenReturn("jwt-token");

        AdminLoginResponse response = authService.adminLogin(1L, "admin", "admin1234");

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(adminUser.getLoginAttempts()).isZero();
        assertThat(adminUser.getLockedUntil()).isNull();
    }

    // UT-AUTH-006: 존재하지 않는 사용자로 로그인
    @Test
    @DisplayName("존재하지 않는 사용자로 로그인하면 ForbiddenException이 발생한다")
    void adminLoginUserNotFound() {
        when(adminUserRepository.findByStoreIdAndUsername(1L, "unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.adminLogin(1L, "unknown", "password"))
                .isInstanceOf(ForbiddenException.class);
    }

    // UT-AUTH-007: 카카오 로그인 성공 (dev-test-token)
    @Test
    @DisplayName("dev-test-token으로 카카오 로그인에 성공한다")
    void kakaoLoginWithDevToken() {
        RestaurantTable table = RestaurantTable.builder().id(1L).tableNumber(1).build();
        TableSession session = TableSession.builder().id(1L).build();
        CustomerProfile profile = CustomerProfile.builder()
                .id(100L).kakaoId(12345L).nickname("테스터").build();

        when(storeService.getStore(1L)).thenReturn(store);
        when(tableService.getTableByStoreAndNumber(1L, 1)).thenReturn(table);
        when(tableSessionService.getOrCreateActiveSession(1L)).thenReturn(session);
        when(customerProfileService.findOrCreate(any(KakaoProfile.class))).thenReturn(profile);
        when(jwtTokenProvider.createToken(anyMap())).thenReturn("customer-jwt");

        KakaoLoginResponse response = authService.kakaoLogin(1L, 1, "dev-test-token");

        assertThat(response.token()).isEqualTo("customer-jwt");
        assertThat(response.sessionId()).isEqualTo(1L);
        assertThat(response.profileId()).isEqualTo(100L);
    }

    // UT-AUTH-008: 카카오 로그인 - 존재하지 않는 테이블
    @Test
    @DisplayName("존재하지 않는 테이블로 카카오 로그인하면 NotFoundException이 발생한다")
    void kakaoLoginTableNotFound() {
        when(storeService.getStore(1L)).thenReturn(store);
        when(tableService.getTableByStoreAndNumber(1L, 99)).thenThrow(new NotFoundException("테이블을 찾을 수 없습니다"));

        assertThatThrownBy(() -> authService.kakaoLogin(1L, 99, "dev-test-token"))
                .isInstanceOf(NotFoundException.class);
    }
}
