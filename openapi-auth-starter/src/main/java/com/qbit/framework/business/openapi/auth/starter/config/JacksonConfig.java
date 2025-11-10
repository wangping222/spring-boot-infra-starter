package com.qbit.framework.business.openapi.auth.starter.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.TimeZone;

public final class JacksonConfig {
    private JacksonConfig() {}

    public static ObjectMapper defaultMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 序列化：忽略为 null 的字段
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 反序列化：忽略未知字段，避免第三方返回新增字段导致失败
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 日期时间：使用 ISO-8601 字符串而不是时间戳
        mapper.registerModule(new JavaTimeModule());
        // 时区：统一使用 UTC（如需本地时区可改为 systemDefault）
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        return mapper;
    }
}