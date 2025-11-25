package com.qbit.framework.test.feign;

import com.qbit.framework.test.feign.api.InternalApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(classes = FeignTest.App.class)
@EnableFeignClients(basePackageClasses = InternalApi.class)
@Slf4j
public class FeignTest {

    @Resource
    InternalApi internalApi;

    static MockWebServer server;

    @BeforeAll
    static void beforeAll() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void afterAll() throws Exception {
        server.shutdown();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("test.internalApiUrl", () -> server.url("/").toString());
    }

    @Test
    public void testApiCall() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setHeader("Content-Type", "text/plain").setBody("ok"));
        InternalApi.SyncTransactionDTO dto = new InternalApi.SyncTransactionDTO();
        dto.setCardTransactionId("test");
        String s = internalApi.transactionSync(dto);
        Assertions.assertEquals("ok", s);
        RecordedRequest req = server.takeRequest();
        Assertions.assertEquals("/api/core/internal/webhook/display_transaction-sync", req.getPath());
        Assertions.assertEquals("POST", req.getMethod());
    }

    @SpringBootApplication
    static class App {

    }

}
