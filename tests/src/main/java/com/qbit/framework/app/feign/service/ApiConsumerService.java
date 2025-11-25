package com.qbit.framework.app.feign.service;

import com.qbit.framework.app.feign.api.InternalApi;
import com.qbit.framework.starter.merchant.properties.FeignApiProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApiConsumerService implements CommandLineRunner {

    @Resource
    InternalApi internalApi;

    @Resource
    FeignApiProperties feignApiProperties;

    @Resource
    Environment environment;

    @Override
    public void run(String... args) throws Exception {
        log.info("env.feign.api.base-url={}, env.secret={}, props.base-url={}, secret is {}",
                environment.getProperty("feign.api.base-url"),
                environment.getProperty("feign.api.secret") != null ? "present" : "null",
                feignApiProperties.getBaseUrl(),
                feignApiProperties.getSecret() != null ? "present" : "null");
        InternalApi.SyncTransactionDTO dto = new InternalApi.SyncTransactionDTO();
        dto.setCardTransactionId("5d2a539e-060b-47e9-b292-dcfa63cf9857");
        String s = internalApi.transactionSync(dto);
        log.info("transaction sync: {}", s);
    }
}
