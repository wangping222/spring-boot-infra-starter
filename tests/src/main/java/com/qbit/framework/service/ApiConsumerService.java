package com.qbit.framework.service;

import com.qbit.framework.api.InternalApi;
import com.qbit.framework.common.web.Result;
import com.qbit.framework.starter.merchant.properties.FeignApiProperties;
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
        log.info("props.base-url={}, secret is {}",
                feignApiProperties.getBaseUrl(),
                feignApiProperties.getSecret() != null ? "present" : "null");
        InternalApi.SyncTransactionDTO dto = new InternalApi.SyncTransactionDTO();
        dto.setCardTransactionId("5d2a539e-060b-47e9-b292-dcfa63cf9857");
        Result<?> s = internalApi.transactionSync(dto);
        log.info("transaction sync: {}", s);
    }
}
