package com.qbit.framework.core.api.model.toolkits.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

/**
 * JSON 工具类
 *  序列化规则与api response 相同
 *
 * @author Qbit
 */
@UtilityClass
public class JSONUtil {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        SerizalizerConfig.customizeBuilder(builder);
        OBJECT_MAPPER = builder.build();
    }

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param value 待序列化对象
     * @return JSON 字符串；当 value 为 null 时返回 null
     * @throws IllegalStateException 当序列化失败时抛出运行时异常
     */
    public static String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON serialize error", e);
        }
    }

    /**
     * 将对象以美化格式序列化为 JSON 字符串（带缩进）。
     *
     * @param value 待序列化对象
     * @return 美化后的 JSON 字符串；当 value 为 null 时返回 null
     * @throws IllegalStateException 当序列化失败时抛出运行时异常
     */
    public static String toJsonPretty(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON serialize pretty error", e);
        }
    }

    /**
     * 将对象序列化为 JSON 二进制数组（UTF-8）。
     *
     * @param value 待序列化对象
     * @return JSON 字节数组；当 value 为 null 时返回 null
     * @throws IllegalStateException 当序列化失败时抛出运行时异常
     */
    public static byte[] toJsonBytes(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON serialize bytes error", e);
        }
    }

    /**
     * 从 JSON 字符串反序列化为对象。
     *
     * @param json JSON 字符串
     * @param type 目标类型
     * @return 目标对象；当 json 为空或仅空白时返回 null
     * @throws IllegalStateException 当反序列化失败时抛出运行时异常
     */
    public static <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON deserialize error", e);
        }
    }

    /**
     * 使用 TypeReference 从 JSON 字符串反序列化为复杂泛型对象。
     *
     * @param json JSON 字符串
     * @param typeRef 目标类型引用，例如 new TypeReference<List<User>>() {}
     * @return 目标对象；当 json 为空或仅空白时返回 null
     * @throws IllegalStateException 当反序列化失败时抛出运行时异常
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON deserialize error", e);
        }
    }

    /**
     * 从 JSON 字节数组反序列化为对象。
     *
     * @param jsonBytes JSON 二进制（UTF-8）
     * @param type 目标类型
     * @return 目标对象；当 jsonBytes 为空时返回 null
     * @throws IllegalStateException 当反序列化失败时抛出运行时异常
     */
    public static <T> T fromJson(byte[] jsonBytes, Class<T> type) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonBytes, type);
        } catch (IOException e) {
            throw new IllegalStateException("JSON deserialize bytes error", e);
        }
    }

    /**
     * 使用 TypeReference 从 JSON 字节数组反序列化为复杂泛型对象。
     *
     * @param jsonBytes JSON 二进制（UTF-8）
     * @param typeRef 目标类型引用，例如 new TypeReference<List<User>>() {}
     * @return 目标对象；当 jsonBytes 为空时返回 null
     * @throws IllegalStateException 当反序列化失败时抛出运行时异常
     */
    public static <T> T fromJson(byte[] jsonBytes, TypeReference<T> typeRef) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonBytes, typeRef);
        } catch (IOException e) {
            throw new IllegalStateException("JSON deserialize bytes error", e);
        }
    }

    /**
     * 使用 Jackson 的 convertValue 进行对象间转换（常用于 Map/JSON Tree -> POJO）。
     *
     * @param from 源对象
     * @param toType 目标类型
     * @return 转换后的对象；当 from 为 null 时返回 null
     */
    public static <T> T convertValue(Object from, Class<T> toType) {
        if (from == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(from, toType);
    }
}
