package com.qbit.framework.api;


import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "internalApi")
public interface InternalApi {
    @Data
    public static class SyncTransactionDTO {
        private String cardTransactionId;
    }

    @PostMapping("/api/core/internal/webhook/display-transaction-sync")
    public String transactionSync(@RequestBody SyncTransactionDTO dto);
}
