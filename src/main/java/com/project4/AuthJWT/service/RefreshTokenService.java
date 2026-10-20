

package com.project4.AuthJWT.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.project4.AuthJWT.DTO.RefreshResponse;
import com.project4.AuthJWT.constants.RefreshTokenStatus;
import com.project4.AuthJWT.entity.AuthUser;
import com.project4.AuthJWT.entity.RefreshTokenEntity;
import com.project4.AuthJWT.exception.InvalidRefreshTokenException;
import com.project4.AuthJWT.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long refreshExpirationMs;
    private final RefreshTokenRevocationService revocationService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,JWTService jwtService,
            RefreshTokenRevocationService revocationService,
            @Value("${jwt.refresh-token-expiration}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
        this.jwtService =jwtService;
        this.revocationService = revocationService;
    }

    @Transactional
    public String createRefreshToken(
            AuthUser user, String deviceId, String deviceName) {

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken);

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setUser(user);
        refreshToken.setDeviceId(deviceId);
        refreshToken.setDeviceName(deviceName);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L));
        refreshToken.setStatus(RefreshTokenStatus.ACTIVE);

        refreshTokenRepository.save(refreshToken);

        // Return the raw token only to the client, never store it in the DB.
        return rawToken;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
    
    public RefreshTokenEntity validateRefreshToken(String token) {
    	String hashedtoken = hashToken(token);
    	
    	RefreshTokenEntity refreshToken =refreshTokenRepository.findByTokenHashForUpdate(hashedtoken)
    			.orElseThrow(()-> new InvalidRefreshTokenException("Invalid Token"));
    	
    	if (RefreshTokenStatus.ROTATED.equals(refreshToken.getStatus())) {
    	    revokeAllUserTokens(refreshToken.getUser().getId());

    	    throw new InvalidRefreshTokenException(
    	            "Refresh token reuse detected. Please log in again."
    	    );
    	}

    	
    	if (!RefreshTokenStatus.ACTIVE.equals(refreshToken.getStatus())) {
            throw new InvalidRefreshTokenException("Refresh token is not active");
        }

        if (!refreshToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }
        
        return refreshToken;
    	
    	
    }
    @Transactional
    public RefreshResponse rotateRefreshToken(String rawToken) {
        RefreshTokenEntity oldToken = validateRefreshToken(rawToken);

        AuthUser user = oldToken.getUser();

        String newRefreshToken = createRefreshToken(
                user,
                oldToken.getDeviceId(),
                oldToken.getDeviceName()
        );

        String newTokenHash = hashToken(newRefreshToken);

        oldToken.setStatus(RefreshTokenStatus.ROTATED);
        oldToken.setReplacedByTokenHash(newTokenHash);
        oldToken.setLastUsedAt(LocalDateTime.now());

        refreshTokenRepository.save(oldToken);

        RefreshResponse response = new RefreshResponse();
        response.setAccess_token(jwtService.generateToken(user.getUsername()));
        response.setRefresh_token(newRefreshToken);
        response.setUsername(user.getUsername());
        response.setMessage("TOKEN REFRESHED SUCCESSFULLY");

        return response;
    }
    
    @Transactional
    public void revokeRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshTokenEntity token = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException("Invalid refresh token"));

        if (token.getStatus() == RefreshTokenStatus.ACTIVE) {
            token.setStatus(RefreshTokenStatus.REVOKED);
            token.setLastUsedAt(LocalDateTime.now());

            refreshTokenRepository.save(token);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllUserTokens(Long userId) {
    	revocationService.revokeAllUserTokens(userId);
    }
}
