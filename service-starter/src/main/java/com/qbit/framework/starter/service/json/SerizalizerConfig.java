package com.qbit.framework.starter.service.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.qbit.framework.starter.service.constants.DateTimeFormatConstants;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class SerizalizerConfig {

    public static void customizeBuilder(Jackson2ObjectMapperBuilder builder) {
        // LocalDateTime -> 时间戳 (long)
        builder.serializerByType(LocalDateTime.class,
                new JsonSerializer<LocalDateTime>() {
                    @Override
                    public void serialize(LocalDateTime value, JsonGenerator gen,
                                          SerializerProvider serializers) throws IOException {
                        gen.writeNumber(value.toInstant(ZoneOffset.UTC).toEpochMilli());
                        // 统一使用 UTC 时区
                    }
                });

        // 反序列化: 时间戳(long) -> LocalDateTime (UTC)
        builder.deserializerByType(LocalDateTime.class,
                new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonParser p,
                                                     DeserializationContext ctxt)
                            throws IOException {
                        long timestamp = p.getLongValue();
                        // 统一使用 UTC 时区
                        return LocalDateTime.ofEpochSecond(timestamp / 1000, (int) (timestamp % 1000 * 1_000_000),
                                ZoneOffset.UTC);
                    }
                });

        // LocalDate -> 字符串(yyyy-MM-dd)
        builder.serializerByType(LocalDate.class,
                new JsonSerializer<LocalDate>() {
                    @Override
                    public void serialize(LocalDate value, JsonGenerator gen,
                                          SerializerProvider serializers) throws IOException {
                        gen.writeString(value.format(DateTimeFormatConstants.DATE_FORMAT));
                    }
                });

        // 反序列化: 字符串(yyyy-MM-dd) 或 时间戳(long) -> LocalDate (UTC)
        builder.deserializerByType(LocalDate.class,
                new JsonDeserializer<LocalDate>() {
                    @Override
                    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt)
                            throws IOException {
                        JsonToken token = p.getCurrentToken();
                        if (token == JsonToken.VALUE_STRING) {
                            String text = p.getText();
                            if (text == null || text.trim().isEmpty()) {
                                return null;
                            }
                            return LocalDate.parse(text.trim(), DateTimeFormatConstants.DATE_FORMAT);
                        } else if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
                            long timestamp = p.getLongValue();
                            return LocalDateTime.ofEpochSecond(timestamp / 1000, (int) (timestamp % 1000 * 1_000_000),
                                    ZoneOffset.UTC).toLocalDate();
                        }
                        return null;
                    }
                });

        // Long -> 字符串
        builder.serializerByType(Long.class,
                new JsonSerializer<Long>() {
                    @Override
                    public void serialize(Long value, JsonGenerator gen,
                                          SerializerProvider serializers) throws IOException {
                        if (value == null) {
                            gen.writeNull();
                        } else {
                            gen.writeString(String.valueOf(value));
                        }
                    }
                });
        // 原始类型 long -> 字符串
        builder.serializerByType(Long.TYPE,
                new JsonSerializer<Long>() {
                    @Override
                    public void serialize(Long value, JsonGenerator gen,
                                          SerializerProvider serializers) throws IOException {
                        if (value == null) {
                            gen.writeNull();
                        } else {
                            gen.writeString(String.valueOf(value));
                        }
                    }
                });

        // BigDecimal -> 字符串（使用 toPlainString 避免科学计数法）
        builder.serializerByType(BigDecimal.class,
                new JsonSerializer<BigDecimal>() {
                    @Override
                    public void serialize(BigDecimal value, JsonGenerator gen,
                                          SerializerProvider serializers) throws IOException {
                        if (value == null) {
                            gen.writeNull();
                        } else {
                            gen.writeString(value.toPlainString());
                        }
                    }
                });

        // Double -> 字符串（使用 BigDecimal 保证精度与避免科学计数法）
        builder.serializerByType(Double.class,
                new JsonSerializer<Double>() {
                    @Override
                    public void serialize(Double value, JsonGenerator gen,
                                          SerializerProvider serializers) throws IOException {
                        if (value == null) {
                            gen.writeNull();
                        } else {
                            gen.writeString(BigDecimal.valueOf(value).toPlainString());
                        }
                    }
                });
        // 原始类型 double -> 字符串
        builder.serializerByType(Double.TYPE,
                new JsonSerializer<Double>() {
                    @Override
                    public void serialize(Double value, JsonGenerator gen,
                                          SerializerProvider serializers) throws IOException {
                        if (value == null) {
                            gen.writeNull();
                        } else {
                            gen.writeString(BigDecimal.valueOf(value).toPlainString());
                        }
                    }
                });

        // BigInteger -> 字符串
        builder.serializerByType(BigInteger.class,
                new JsonSerializer<BigInteger>() {
                    @Override
                    public void serialize(BigInteger value, JsonGenerator gen,
                                          SerializerProvider serializers) throws IOException {
                        if (value == null) {
                            gen.writeNull();
                        } else {
                            gen.writeString(value.toString());
                        }
                    }
                });


        builder.featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //
        builder.featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // null 字段不序列化
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
