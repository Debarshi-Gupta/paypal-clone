package com.paypal.transaction_service.service.feign;

import com.paypal.transaction_service.config.FeignConfig;
import com.paypal.transaction_service.model.dto.DepositRequest;
import com.paypal.transaction_service.model.dto.DepositResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "wallet-service", url = "${wallet.service.url}",  configuration = FeignConfig.class)
public interface WalletClient {

    @GetMapping("/api/wallets/balance")
    BigDecimal getBalance(@RequestParam Long userId);

    @PostMapping("/api/wallets/deposit")
    DepositResponse deposit(@RequestParam Long userId, @RequestBody DepositRequest request);
}
