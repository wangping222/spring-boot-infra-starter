package com.qbit.framework.app.feign.service;

import com.qbit.framework.app.feign.api.InternalApi;
import com.qbit.framework.business.merchant.starter.properties.FeignApiProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApiConsumerService implements CommandLineRunner {

    @Resource
    InternalApi internalApi;

    @Resource
    FeignApiProperties feignApiProperties;

    @Override
    public void run(String... args) throws Exception {
        log.info("feign.api.base-url={}, secret is {}",
                feignApiProperties.getBaseUrl(),
                feignApiProperties.getSecret() != null ? "present" : "null");
        InternalApi.SyncTransactionDTO dto = new InternalApi.SyncTransactionDTO();
        dto.setCardTransactionId("5d2a539e-060b-47e9-b292-dcfa63cf9857");
        String s = internalApi.transactionSync(dto);
        log.info("transaction sync: {}", s);
    }
}
