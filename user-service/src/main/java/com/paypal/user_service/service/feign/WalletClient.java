package com.paypal.user_service.service.feign;

import com.paypal.user_service.config.AppConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "wallet-service", url = "${wallet.service.url}", configuration = AppConfig.class)
public interface WalletClient {

    @PostMapping("/api/wallets/create")
    void createWallet(@RequestParam Long userId);
}
