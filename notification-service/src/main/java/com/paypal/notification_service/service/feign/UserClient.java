package com.paypal.notification_service.service.feign;

import com.paypal.notification_service.config.FeignConfig;
import com.paypal.notification_service.model.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user.service.url}",  configuration = FeignConfig.class)
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable Long id);
}
