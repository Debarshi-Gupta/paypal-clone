package com.paypal.transaction_service.service.feign;

import com.paypal.transaction_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "wallet-service", url = "${wallet.service.url}",  configuration = FeignConfig.class)
public interface WalletClient {

    @PostMapping("/api/wallets/transfer")
    void transfer(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam BigDecimal amount);
}
