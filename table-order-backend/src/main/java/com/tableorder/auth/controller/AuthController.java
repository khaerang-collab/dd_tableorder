package com.tableorder.auth.controller;

import com.tableorder.auth.dto.*;
import com.tableorder.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/admin/auth/login")
    public ResponseEntity<AdminLoginResponse> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(authService.adminLogin(request.storeId(), request.username(), request.password()));
    }

    @PostMapping("/customer/auth/kakao-login")
    public ResponseEntity<KakaoLoginResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return ResponseEntity.ok(authService.kakaoLogin(
                request.storeId(), request.tableNumber(), request.kakaoAccessToken()));
    }
}
