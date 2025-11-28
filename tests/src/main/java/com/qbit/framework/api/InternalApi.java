package com.qbit.framework.api;


import com.qbit.framework.common.web.Result;
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
    Result<?> transactionSync(@RequestBody SyncTransactionDTO dto);
}
