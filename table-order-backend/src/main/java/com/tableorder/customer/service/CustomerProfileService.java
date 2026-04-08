package com.tableorder.customer.service;

import com.tableorder.auth.dto.KakaoProfile;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.customer.entity.CustomerProfile;
import com.tableorder.customer.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerProfileRepository repository;

    @Transactional
    public CustomerProfile findOrCreate(KakaoProfile kakao) {
        return repository.findByKakaoId(kakao.kakaoId())
                .map(profile -> {
                    profile.setNickname(kakao.nickname());
                    profile.setGender(kakao.gender());
                    profile.setAgeRange(kakao.ageRange());
                    profile.setProfileImageUrl(kakao.profileImageUrl());
                    profile.setVisitCount(profile.getVisitCount() + 1);
                    profile.setLastVisitAt(LocalDateTime.now());
                    return profile;
                })
                .orElseGet(() -> repository.save(CustomerProfile.builder()
                        .kakaoId(kakao.kakaoId())
                        .nickname(kakao.nickname())
                        .gender(kakao.gender())
                        .ageRange(kakao.ageRange())
                        .profileImageUrl(kakao.profileImageUrl())
                        .visitCount(1)
                        .lastVisitAt(LocalDateTime.now())
                        .build()));
    }

    @Transactional
    public void addOrderAmount(Long profileId, int amount) {
        repository.findById(profileId).ifPresent(p ->
                p.setTotalOrderAmount(p.getTotalOrderAmount() + amount));
    }

    public CustomerProfile getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("고객 프로필을 찾을 수 없습니다"));
    }
}
