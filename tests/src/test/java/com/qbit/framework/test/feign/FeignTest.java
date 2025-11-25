package com.qbit.framework.test.feign;

import com.qbit.framework.business.merchant.starter.config.FeignAutoConfiguration;
import com.qbit.framework.test.feign.api.InternalApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@SpringBootTest(classes = FeignTest.App.class)
@Slf4j
public class FeignTest {

    @Resource
    InternalApi internalApi;

    @Test
    public void testApiCallReal() {
        InternalApi.SyncTransactionDTO dto = new InternalApi.SyncTransactionDTO();
        dto.setCardTransactionId("5d2a539e-060b-47e9-b292-dcfa63cf9857");
        String s = internalApi.transactionSync(dto);
        Assertions.assertNotNull(s);
    }

    @Profile("test")
    @SpringBootApplication
    @Import(FeignAutoConfiguration.class)
    @EnableFeignClients(basePackageClasses = {InternalApi.class})
    static class App {

    }

}
