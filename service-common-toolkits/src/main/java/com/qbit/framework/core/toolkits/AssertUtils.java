package com.qbit.framework.core.toolkits;

import com.qbit.framework.core.toolkits.exception.factory.CustomerExceptionFactory;
import com.qbit.framework.core.toolkits.exception.CustomerException;
import com.qbit.framework.core.toolkits.message.MessageFormatter;
import com.qbit.framework.core.toolkits.message.MessagePlaceholder;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * @author Qbit Framework
 *
 */
public final class AssertUtils {
    private AssertUtils() {
        throw new AssertionError();
    }

    public static void state(boolean expression, Supplier<CustomerException> exceptionSupplier) {
        if (!expression) {
            throw Objects.requireNonNull(nullSafeGet(exceptionSupplier), "exception must not null");
        }
    }

    public static void isTrue(boolean expression, MessagePlaceholder placeholder) {
        isTrue(expression, placeholder.getPattern(), placeholder.getArgs());
    }

    public static void isTrue(boolean expression, String message, Object... args) {
        if (!expression) {
            throw CustomerExceptionFactory.businessMessage(MessageFormatter.java().format(message, args));
        }
    }

    public static void isTrue(boolean expression, Supplier<String> messageSupplier) {
        state(expression, () -> CustomerExceptionFactory.businessMessage(nullSafeGet(messageSupplier)));
    }

    public static void isFalse(boolean expression, MessagePlaceholder placeholder) {
        isFalse(expression, placeholder.getPattern(), placeholder.getArgs());
    }

    public static void isFalse(boolean expression, String message, Object... args) {
        isTrue(!expression, message, args);
    }

    public static void isFalse(boolean expression, Supplier<String> messageSupplier) {
        isTrue(!expression, messageSupplier);
    }

    public static void isNull(@Nullable Object object, String message, Object... args) {
        isTrue(object == null, message, args);
    }

    public static void isNull(@Nullable Object object, Supplier<String> messageSupplier) {
        isTrue(object == null, messageSupplier);
    }

    public static void notNull(Object object, MessagePlaceholder placeholder) {
        notNull(object, placeholder.getPattern(), placeholder.getArgs());
    }

    public static void notNull(@Nullable Object object, String message, Object... args) {
        isTrue(object != null, message, args);
    }

    public static void notNull(@Nullable Object object, Supplier<String> messageSupplier) {
        isTrue(object != null, messageSupplier);
    }

    public static void hasLength(String text, MessagePlaceholder placeholder) {
        hasLength(text, placeholder.getPattern(), placeholder.getArgs());
    }

    public static void hasLength(@Nullable String text, String message, Object... args) {
        isTrue(StringUtils.hasLength(text), message, args);
    }

    public static void hasLength(@Nullable String text, Supplier<String> messageSupplier) {
        isTrue(StringUtils.hasLength(text), messageSupplier);
    }

    public static void hasText(@Nullable String text, String message, Object... args) {
        isTrue(StringUtils.hasText(text), message, args);
    }

    public static void hasText(@Nullable String text, Supplier<String> messageSupplier) {
        isTrue(StringUtils.hasText(text), messageSupplier);
    }

    public static void doesNotContain(@Nullable String textToSearch, String substring, String message, Object... args) {
        isFalse(StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) && textToSearch.contains(substring), message, args);
    }

    public static void doesNotContain(@Nullable String textToSearch, String substring, Supplier<String> messageSupplier) {
        isFalse(StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) && textToSearch.contains(substring), messageSupplier);
    }

    public static void notEmpty(@Nullable Object[] array, MessagePlaceholder placeholder) {
        notEmpty(array, placeholder.getPattern(), placeholder.getArgs());
    }

    public static void notEmpty(@Nullable Object[] array, String message, Object... args) {
        isFalse(ObjectUtils.isEmpty(array), message, args);
    }

    public static void notEmpty(@Nullable Object[] array, Supplier<String> messageSupplier) {
        isFalse(ObjectUtils.isEmpty(array), messageSupplier);
    }

    public static void notEmpty(@Nullable Collection<?> collection, MessagePlaceholder placeholder) {
        notEmpty(collection, placeholder.getPattern(), placeholder.getArgs());
    }

    public static void notEmpty(@Nullable Collection<?> collection, String message, Object... args) {
        isFalse(CollectionUtils.isEmpty(collection), message, args);
    }

    public static void notEmpty(@Nullable Collection<?> collection, Supplier<String> messageSupplier) {
        isFalse(CollectionUtils.isEmpty(collection), messageSupplier);
    }

    public static void noNullElements(@Nullable Collection<?> collection, String message, Object... args) {
        if (collection != null) {
            for (Object element : collection) {
                if (element == null) {
                    throw CustomerExceptionFactory.businessMessage(MessageFormatter.java().format(message, args));
                }
            }
        }

    }


    public static void notEmpty(@Nullable Map<?, ?> map, MessagePlaceholder placeholder) {
        notEmpty(map, placeholder.getPattern(), placeholder.getArgs());
    }

    public static void notEmpty(@Nullable Map<?, ?> map, String message, Object... args) {
        isFalse(CollectionUtils.isEmpty(map), message, args);
    }

    public static void notEmpty(@Nullable Map<?, ?> map, Supplier<String> messageSupplier) {
        isFalse(CollectionUtils.isEmpty(map), messageSupplier);
    }

    private static boolean endsWithSeparator(String msg) {
        return msg.endsWith(":") || msg.endsWith(";") || msg.endsWith(",") || msg.endsWith(".");
    }

    private static String messageWithTypeName(String msg, @Nullable Object typeName) {
        return msg + (msg.endsWith(" ") ? "" : ": ") + typeName;
    }

    @Nullable
    private static <T> T nullSafeGet(@Nullable Supplier<T> messageSupplier) {
        return (T) (messageSupplier != null ? messageSupplier.get() : null);
    }

    /**
     * 断言字符串匹配指定的正则表达式
     *
     * @param text    待检查的字符串
     * @param regex   正则表达式
     * @param message 不匹配时的错误消息
     * @param args    错误消息参数
     * @throws CustomerException 当字符串不匹配正则表达式时
     */
    public static void matchesPattern(@Nullable String text, String regex, String message, Object... args) {
        notNull(regex, "正则表达式不能为空");
        isTrue(text != null && Pattern.matches(regex, text), message, args);
    }

    /**
     * 断言字符串匹配指定的正则表达式
     *
     * @param text        待检查的字符串
     * @param regex       正则表达式
     * @param placeholder 不匹配时的错误消息占位符
     * @throws CustomerException 当字符串不匹配正则表达式时
     */
    public static void matchesPattern(@Nullable String text, String regex, MessagePlaceholder placeholder) {
        matchesPattern(text, regex, placeholder.getPattern(), placeholder.getArgs());
    }

    /**
     * 断言字符串匹配指定的正则表达式
     *
     * @param text    待检查的字符串
     * @param pattern 已编译的正则表达式模式
     * @param message 不匹配时的错误消息
     * @param args    错误消息参数
     * @throws CustomerException 当字符串不匹配正则表达式时
     */
    public static void matchesPattern(@Nullable String text, Pattern pattern, String message, Object... args) {
        notNull(pattern, "正则表达式模式不能为空");
        isTrue(text != null && pattern.matcher(text).matches(), message, args);
    }

    /**
     * 断言数值在指定范围内
     *
     * @param value   待检查的数值
     * @param min     最小值（包含）
     * @param max     最大值（包含）
     * @param message 不在范围内时的错误消息
     * @param args    错误消息参数
     * @throws CustomerException 当数值不在指定范围内时
     */
    public static void isInRange(int value, int min, int max, String message, Object... args) {
        isTrue(value >= min && value <= max, message, args);
    }

    /**
     * 断言数值在指定范围内
     *
     * @param value       待检查的数值
     * @param min         最小值（包含）
     * @param max         最大值（包含）
     * @param placeholder 不在范围内时的错误消息占位符
     * @throws CustomerException 当数值不在指定范围内时
     */
    public static void isInRange(int value, int min, int max, MessagePlaceholder placeholder) {
        isInRange(value, min, max, placeholder.getPattern(), placeholder.getArgs());
    }

    /**
     * 断言数值在指定范围内（长整型）
     *
     * @param value   待检查的数值
     * @param min     最小值（包含）
     * @param max     最大值（包含）
     * @param message 不在范围内时的错误消息
     * @param args    错误消息参数
     * @throws CustomerException 当数值不在指定范围内时
     */
    public static void isInRange(long value, long min, long max, String message, Object... args) {
        isTrue(value >= min && value <= max, message, args);
    }

    /**
     * 断言数值在指定范围内（浮点型）
     *
     * @param value   待检查的数值
     * @param min     最小值（包含）
     * @param max     最大值（包含）
     * @param message 不在范围内时的错误消息
     * @param args    错误消息参数
     * @throws CustomerException 当数值不在指定范围内时
     */
    public static void isInRange(double value, double min, double max, String message, Object... args) {
        isTrue(value >= min && value <= max, message, args);
    }

    /**
     * 断言字符串包含指定的子串
     *
     * @param text      待检查的字符串
     * @param substring 期望包含的子串
     * @param message   不包含时的错误消息
     * @param args      错误消息参数
     * @throws CustomerException 当字符串不包含指定子串时
     */
    public static void contains(@Nullable String text, String substring, String message, Object... args) {
        notNull(substring, "子串不能为空");
        isTrue(StringUtils.hasLength(text) && text.contains(substring), message, args);
    }

    /**
     * 断言字符串包含指定的子串
     *
     * @param text        待检查的字符串
     * @param substring   期望包含的子串
     * @param placeholder 不包含时的错误消息占位符
     * @throws CustomerException 当字符串不包含指定子串时
     */
    public static void contains(@Nullable String text, String substring, MessagePlaceholder placeholder) {
        contains(text, substring, placeholder.getPattern(), placeholder.getArgs());
    }

    /**
     * 断言集合中包含指定的元素
     *
     * @param collection 待检查的集合
     * @param element    期望包含的元素
     * @param message    不包含时的错误消息
     * @param args       错误消息参数
     * @throws CustomerException 当集合不包含指定元素时
     */
    public static void contains(@Nullable Collection<?> collection, Object element, String message, Object... args) {
        notNull(element, "元素不能为空");
        isTrue(collection != null && collection.contains(element), message, args);
    }

    /**
     * 断言集合中包含指定的元素
     *
     * @param collection  待检查的集合
     * @param element     期望包含的元素
     * @param placeholder 不包含时的错误消息占位符
     * @throws CustomerException 当集合不包含指定元素时
     */
    public static void contains(@Nullable Collection<?> collection, Object element, MessagePlaceholder placeholder) {
        contains(collection, element, placeholder.getPattern(), placeholder.getArgs());
    }

    /**
     * 断言字符串长度不超过最大长度
     *
     * @param text      待检查的字符串
     * @param maxLength 最大长度
     * @param message   超长时的错误消息
     * @param args      错误消息参数
     * @throws CustomerException 当字符串长度超过最大长度时
     */
    public static void maxLength(@Nullable String text, int maxLength, String message, Object... args) {
        isTrue(text == null || text.length() <= maxLength, message, args);
    }

    /**
     * 断言字符串长度不超过最大长度
     *
     * @param text        待检查的字符串
     * @param maxLength   最大长度
     * @param placeholder 超长时的错误消息占位符
     * @throws CustomerException 当字符串长度超过最大长度时
     */
    public static void maxLength(@Nullable String text, int maxLength, MessagePlaceholder placeholder) {
        maxLength(text, maxLength, placeholder.getPattern(), placeholder.getArgs());
    }

    /**
     * 断言字符串长度不小于最小长度
     *
     * @param text      待检查的字符串
     * @param minLength 最小长度
     * @param message   长度不足时的错误消息
     * @param args      错误消息参数
     * @throws CustomerException 当字符串长度小于最小长度时
     */
    public static void minLength(@Nullable String text, int minLength, String message, Object... args) {
        isTrue(text != null && text.length() >= minLength, message, args);
    }

    /**
     * 断言字符串长度不小于最小长度
     *
     * @param text        待检查的字符串
     * @param minLength   最小长度
     * @param placeholder 长度不足时的错误消息占位符
     * @throws CustomerException 当字符串长度小于最小长度时
     */
    public static void minLength(@Nullable String text, int minLength, MessagePlaceholder placeholder) {
        minLength(text, minLength, placeholder.getPattern(), placeholder.getArgs());
    }
}
