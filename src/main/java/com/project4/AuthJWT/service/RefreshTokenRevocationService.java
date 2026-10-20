package com.project4.AuthJWT.service;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.project4.AuthJWT.constants.RefreshTokenStatus;
import com.project4.AuthJWT.entity.RefreshTokenEntity;
import com.project4.AuthJWT.repository.RefreshTokenRepository;

@Service
public class RefreshTokenRevocationService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenRevocationService(
            RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllUserTokens(Long userId) {

        List<RefreshTokenEntity> tokens =
                refreshTokenRepository.findByUser_IdAndStatus(
                        userId,
                        RefreshTokenStatus.ACTIVE
                );

        for (RefreshTokenEntity token : tokens) {
            token.setStatus(RefreshTokenStatus.REVOKED);
            token.setLastUsedAt(LocalDateTime.now());
        }

        refreshTokenRepository.saveAll(tokens);
    }
}