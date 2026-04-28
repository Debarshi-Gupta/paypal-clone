package com.paypal.notification_service.service.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Map;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {

        try (InputStream bodyIs = response.body().asInputStream()) {

            Map<Object, Object> errorMap = objectMapper.readValue(bodyIs, Map.class);

            String message = (String) errorMap.getOrDefault("message", "Unknown error");

            log.error("Feign error from {}: {}", methodKey, message);

            return new RuntimeException(message);

        } catch (Exception e) {
            log.error("Error decoding Feign response", e);
            return new RuntimeException("External service error");
        }
    }
}
