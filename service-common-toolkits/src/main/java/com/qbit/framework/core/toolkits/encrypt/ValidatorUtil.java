package com.qbit.framework.core.toolkits.encrypt;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * @author LiTao litao@qbitnetwork.com
 */
public class ValidatorUtil {
    @NotNull
    private static final Pattern UUID = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Pattern UUID_V3 = Pattern.compile("^[0-9A-F]{8}-[0-9A-F]{4}-3[0-9A-F]{3}-[0-9A-F]{4}-[0-9A-F]{12}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern UUID_V4 = Pattern.compile("^[0-9A-F]{8}-[0-9A-F]{4}-4[0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern UUID_V5 = Pattern.compile("^[0-9A-F]{8}-[0-9A-F]{4}-5[0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMAIL = Pattern.compile("^([A-Za-z0-9_\\-\\+.])+@([A-Za-z0-9_\\-.])+\\.([A-Za-z]{2,9})$");
    private static final Pattern HASH_32 = Pattern.compile("^[0-9a-fA-F]{32}$");

    private static final Pattern CHINESE = Pattern.compile("[\\u4e00-\\u9fa5]");


    public static boolean validator(Pattern reg, String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        return reg.matcher(value).matches();
    }

    /**
     * 验证uuid格式
     *
     * @param uuid uuid
     * @return boolean
     */
    public static boolean isUUID(String uuid) {
        return validator(UUID, uuid);
    }

    /**
     * 验证指定版本的uuid格式
     *
     * @param uuid    uuid
     * @param version uuid version
     * @return boolean
     */
    public static boolean isUUID(String uuid, int version) {
        return switch (version) {
            case 3 -> validator(UUID_V3, uuid);
            case 4 -> validator(UUID_V4, uuid);
            case 5 -> validator(UUID_V5, uuid);
            default -> validator(UUID, uuid);
        };
    }

    /**
     * 验证邮箱格式
     *
     * @param email email
     * @return boolean
     */
    public static boolean isEmail(String email) {
        return validator(EMAIL, email);
    }

    public static boolean isInnerEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }

        return email.toLowerCase().endsWith("@qbitnetwork.com") || email.toLowerCase().endsWith("@interlace.money");
    }

    /**
     * 验证hash长度是否为32位
     *
     * @param hash hash
     * @return boolean
     */
    public static boolean isHash32(String hash) {
        return validator(HASH_32, hash);
    }

    /**
     * 检测是否存在中文
     *
     * @param content content
     * @return boolean
     */
    public static boolean isChinese(String content) {
        return validator(CHINESE, content);
    }


}
