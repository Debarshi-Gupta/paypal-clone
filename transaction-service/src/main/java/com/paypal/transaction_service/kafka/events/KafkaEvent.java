package com.paypal.transaction_service.kafka.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "eventType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TransferSucceededEvent.class, name = "TRANSFER_SUCCEEDED"),
        @JsonSubTypes.Type(value = TransferFailedEvent.class, name = "TRANSFER_FAILED")
})
public abstract class KafkaEvent {

    private KafkaEventType eventType;
}
