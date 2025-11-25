package com.qbit.framework.app.feign;

import com.qbit.framework.business.merchant.starter.config.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.qbit.framework.business.merchant.starter.properties.FeignApiProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(FeignApiProperties.class)
@ConfigurationPropertiesScan
@Import({FeignAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
