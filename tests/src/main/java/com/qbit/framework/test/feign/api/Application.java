package com.qbit.framework.test.feign.api;

import com.qbit.framework.business.merchant.starter.config.FeignAutoConfiguration;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@SpringBootApplication
@Import(FeignAutoConfiguration.class)
@EnableFeignClients(basePackageClasses = {InternalApi.class})
public class Application implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Resource
    InternalApi internalApi;

    @Override
    public void run(String... args) throws Exception {
        InternalApi.SyncTransactionDTO dto = new InternalApi.SyncTransactionDTO();
        dto.setCardTransactionId("5d2a539e-060b-47e9-b292-dcfa63cf9857");
        String s = internalApi.transactionSync(dto);
    }
}
