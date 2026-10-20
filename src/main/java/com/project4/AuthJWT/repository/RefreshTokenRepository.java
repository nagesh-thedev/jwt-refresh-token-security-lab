package com.project4.AuthJWT.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project4.AuthJWT.constants.RefreshTokenStatus;
import com.project4.AuthJWT.entity.RefreshTokenEntity;

import jakarta.persistence.LockModeType;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshTokenEntity, Long> {

    //Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    //For Safe Update at concurrency mode
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RefreshTokenEntity t WHERE t.tokenHash = :tokenHash")
    Optional<RefreshTokenEntity> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    List<RefreshTokenEntity> findByUser_IdAndStatus(Long userId,RefreshTokenStatus status);
}