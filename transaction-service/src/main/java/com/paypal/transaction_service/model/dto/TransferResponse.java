package com.paypal.transaction_service.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {

    private Long transfer_id;
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;
    private String status;
    private String description;
    private LocalDateTime createdAt;
}