package com.umaso.mantenimientos.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanup {
    private final RefreshTokenService refreshTokenService;

    @Scheduled(cron = "${app.security.refresh-cleanup-cron:0 15 3 * * *}", zone = "UTC")
    public void purgeExpiredTokens() { refreshTokenService.purgeExpired(); }
}
