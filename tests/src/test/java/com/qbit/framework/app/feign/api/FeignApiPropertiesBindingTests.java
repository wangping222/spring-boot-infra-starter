package com.qbit.framework.app.feign.api;

import com.qbit.framework.business.merchant.starter.properties.FeignApiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FeignApiPropertiesBindingTests.Config.class)
@TestPropertySource(locations = "classpath:application.yml")
class FeignApiPropertiesBindingTests {

    @Configuration
    @EnableConfigurationProperties(FeignApiProperties.class)
    static class Config { }

    @Autowired
    FeignApiProperties props;

    @Test
    void bindsFromApplicationYaml() {
        assertNotNull(props.getBaseUrl());
        assertNotNull(props.getSecret());
    }
}

